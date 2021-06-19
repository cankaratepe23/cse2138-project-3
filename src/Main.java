import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum CacheType {
    L1D,
    L1I,
    L2
}

public class Main {
    public static final StringBuilder statTrek = new StringBuilder();
    ///L1s
    private static int l1SetIndexBitCount;
    ///L1E
    private static int l1LinesPerSet;
    ///L1b
    private static int l1BlockBits;
    ///L2s
    private static int l2SetIndexBitCount;
    ///L2E
    private static int l2LinesPerSet;
    ///L2b
    private static int l2BlockBits;
    private static String traceFilename;
    private static byte[] ram;
    private static List<Set> l1InstructionCache;
    private static List<Set> l1DataCache;
    private static List<Set> l2Cache;

    ///L1 - Number of sets
    private static int getL1SetCount() {
        return (int) Math.pow(2, l1SetIndexBitCount);
    }

    ///L1 - Size of block (Number of bytes in block)
    private static int getL1BlockSize() {
        return (int) Math.pow(2, l1BlockBits);
    }

    ///L2 - Number of sets
    private static int getL2SetCount() {
        return (int) Math.pow(2, l2SetIndexBitCount);
    }

    ///L2 - Size of block (Number of bytes in block)
    private static int getL2BlockSize() {
        return (int) Math.pow(2, l2BlockBits);
    }

    public static void main(String[] args) {
        traceFilename = "traces/test.trace";
        parseArguments(args);
        initRam("RAM.dat");
        readTrace(traceFilename);
        String stringOfADown = String.format("GNU/linux> ./your_simulator -L1s %d -L1E %d -L1b %d -L2s %d -L2E %d -L2b %d -t %s\n", l1SetIndexBitCount, l1LinesPerSet, l1BlockBits, l2SetIndexBitCount, l2LinesPerSet, l2BlockBits, traceFilename) + "\tL1I-hits:" + HitOrMissEvictionCounter.getInstance(CacheType.L1I).getHit() +
                " L1I-misses:" + HitOrMissEvictionCounter.getInstance(CacheType.L1I).getMiss() +
                " L1I-evictions:" + HitOrMissEvictionCounter.getInstance(CacheType.L1I).getEviction() + "\n" +
                "\tL1D-hits:" + HitOrMissEvictionCounter.getInstance(CacheType.L1D).getHit() +
                " L1D-misses:" + HitOrMissEvictionCounter.getInstance(CacheType.L1D).getMiss() +
                " L1D-evictions:" + HitOrMissEvictionCounter.getInstance(CacheType.L1D).getEviction() + "\n" +
                "\tL2-hits:" + HitOrMissEvictionCounter.getInstance(CacheType.L2).getHit() +
                " L2-misses:" + HitOrMissEvictionCounter.getInstance(CacheType.L2).getMiss() +
                " L2-evictions:" + HitOrMissEvictionCounter.getInstance(CacheType.L2).getEviction() + "\n";
        System.out.println(stringOfADown);
        System.out.print(statTrek);
    }

    // Start trace-parsing functions
    private static void readTrace(String filename) {
        Path filepath = FileSystems.getDefault().getPath(filename);
        if (!Files.exists(filepath)) {
            System.err.printf("File not found: %s\n", filename);
        }
        Scanner traceReader = null;
        try {
            traceReader = new Scanner(filepath);
        } catch (IOException ex) {
            System.err.printf("IO error while reading trace file %s\n", filename);
            ex.printStackTrace();
            System.exit(ex.hashCode());
        }
        while (traceReader.hasNextLine()) {
            String line = traceReader.nextLine();
            parseTraceLine(line);
        }
    }

    private static void parseTraceLine(String line) {
        statTrek.append(line).append("\n");
        char operation = line.charAt(0);
        String sAddress = line.substring(2, 10);
        long address = Long.parseLong(sAddress, 16) % ram.length;
        if (operation == 'I' || operation == 'L') { // format is: op address, size
            executeOperation(operation, address);
        } else if (operation == 'M' || operation == 'S') { // format is: op address, size, data
            String sSize = line.substring(line.indexOf(',') + 2, line.lastIndexOf(','));
            String sData = line.substring(line.lastIndexOf(',') + 2);
            int size = Integer.parseInt(sSize);
            byte[] data = new byte[sData.length() / 2]; // should probably initialize with maximum 8 bytes of capacity...
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) Integer.parseInt(sData.substring(i * 2, (i * 2) + 2), 16);
            }
            executeOperation(operation, address, size, data);
        } else {
            System.err.printf("Invalid data found in trace:\n%s", line);
            System.exit(-1);
        }
    }
    // End


    // Start operation logic
    private static void executeOperation(char operation, long address) {
        TraceDTO L1DTraceDTO = findSet(address, CacheType.L1D);
        TraceDTO L1ITraceDTO = findSet(address, CacheType.L1I);
        TraceDTO L2TraceDTO = findSet(address, CacheType.L2);
        statTrek.append("\t");
        if (operation == 'I') {
            loadInstruction(address, L1ITraceDTO, L2TraceDTO);
        } else if (operation == 'L') {
            loadData(address, L1DTraceDTO, L2TraceDTO);
        }
    }

    private static void executeOperation(char operation, long address, int size, byte[] data) {
        TraceDTO L1DTraceDTO = findSet(address, CacheType.L1D);
        TraceDTO L2TraceDTO = findSet(address, CacheType.L2);
        statTrek.append("\t");
        if (operation == 'S') {
            storeData(address, size, data, L1DTraceDTO, L2TraceDTO);
        } else if (operation == 'M') {
            modifyData(address, size, data, L1DTraceDTO, L2TraceDTO);
        }
    }

    private static void loadInstruction(long address, TraceDTO L1I, TraceDTO L2) {
        StringBuilder messageInABottle = new StringBuilder("\t");
        if (isInCache(L1I)) {
            statTrek.append("L1I hit, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1I).increaseHit();
        } else {
            statTrek.append("L1I miss, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1I).increaseMiss();
            byte[] data = getData(address, CacheType.L1I, L1I.getBlockOffset());
            L1I.getSet().write(data, L1I.getTag(), CacheType.L1I);
            messageInABottle.append("place in L1I set ").append(l1InstructionCache.indexOf(L1I.getSet())).append(", ");
        }

        if (isInCache(L2)) {
            statTrek.append("L2 hit");
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseHit();
        } else {
            statTrek.append("L2 miss");
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseMiss();
            byte[] data = getData(address, CacheType.L2, L2.getBlockOffset());
            L2.getSet().write(data, L2.getTag(), CacheType.L2);
            messageInABottle.append("place in L2 set ").append(l2Cache.indexOf(L2.getSet())).append("\n");
        }
        statTrek.append("\n").append(messageInABottle).append("\n");
    }

    private static void loadData(long address, TraceDTO L1D, TraceDTO L2) {
        StringBuilder myNameJeff = new StringBuilder("\t");
        if (isInCache(L1D)) {
            statTrek.append("L1D hit, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1D).increaseHit();
        } else {
            statTrek.append("L1D miss, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1D).increaseMiss();
            byte[] data = getData(address, CacheType.L1D, L1D.getBlockOffset());
            L1D.getSet().write(data, L1D.getTag(), CacheType.L1D);
            myNameJeff.append("place in L1D set ").append(l1DataCache.indexOf(L1D.getSet())).append(", ");
        }

        if (isInCache(L2)) {
            statTrek.append("L2 hit, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseHit();
        } else {
            statTrek.append("L2 miss, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseMiss();
            byte[] data = getData(address, CacheType.L2, L2.getBlockOffset());
            L2.getSet().write(data, L2.getTag(), CacheType.L2);
            myNameJeff.append("place in L2 set ").append(l2Cache.indexOf(L2.getSet())).append(", ");
        }
        statTrek.append("\n").append(myNameJeff).append("\n");
    }

    private static void storeData(long address, int size, byte[] data, TraceDTO L1D, TraceDTO L2) {
        StringBuilder trippleginton = new StringBuilder("\tStore in ");
        Line line1D = findLine(L1D);
        Line line2 = findLine(L2);
        if (line1D != null) {
            statTrek.append("L1D hit, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1D).increaseHit();
            if (size >= 0) System.arraycopy(data, 0, line1D.data, L1D.getBlockOffset(), size);
            trippleginton.append("L1D, ");
        } else {
            statTrek.append("L1D miss, ");
            HitOrMissEvictionCounter.getInstance(CacheType.L1D).increaseMiss();
        }
        if (line2 != null) {
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseHit();
            if (size >= 0) System.arraycopy(data, 0, line2.data, L2.getBlockOffset(), size);
            trippleginton.append("L2, ");
        } else {
            HitOrMissEvictionCounter.getInstance(CacheType.L2).increaseMiss();
        }
        setData(address, size, data);
        trippleginton.append("RAM");
        statTrek.append("\n").append(trippleginton).append("\n");
    }


    private static void modifyData(long address, int size, byte[] data, TraceDTO L1D, TraceDTO L2) {
        loadData(address, L1D, L2);
        statTrek.append("\t");
        storeData(address, size, data, L1D, L2);
        statTrek.append("\n");
    }
    // End

    private static byte[] getData(long address, CacheType type, int blockOffset) {
        int length = type == CacheType.L2 ? getL2BlockSize() : getL1BlockSize();
        int start = (int) (address - blockOffset);
        byte[] result = new byte[length];

        System.arraycopy(ram, start, result, 0, length);
        return result;
    }

    private static void setData(long address, int size, byte[] data) {
        for (int i = 0; i < size; i++) {
            ram[(int) (address + i)] = data[i];
        }
    }

    // TODO Do not forget to output to RAM.dat file at least once before exit.

    private static boolean isInCache(TraceDTO traceDTO) {
        for (Line line : traceDTO.getSet().lines) {
            if (line.valid && line.tag == traceDTO.getTag())
                return true;
        }
        return false;
    }

    private static Line findLine(TraceDTO traceDTO) {
        for (Line line : traceDTO.getSet().lines) {
            if (line.valid && line.tag == traceDTO.getTag())
                return line;
        }
        return null;
    }

    // Start cache related functions
    private static TraceDTO findSet(long address, CacheType cacheType) {
        String binaryAddress = String.format("%32s", Long.toBinaryString(address)).replace(' ', '0');
        int offset;
        int tag;
        int blockOffset;
        String set;
        switch (cacheType) {
            case L1I -> {
                offset = l1BlockBits + l1SetIndexBitCount;
                tag = Integer.parseInt(binaryAddress.substring(0, binaryAddress.length() - offset), 2);
                blockOffset = Integer.parseInt(binaryAddress.substring(binaryAddress.length() - l1BlockBits), 2);
                if (l1SetIndexBitCount == 0)
                    return new TraceDTO(l1InstructionCache.get(0), tag, blockOffset);
                set = binaryAddress.substring(binaryAddress.length() - offset,
                        binaryAddress.length() - l1BlockBits + 1);
                return new TraceDTO(l1InstructionCache.get(Integer.parseInt(set, 2)), tag, blockOffset);
            }
            case L1D -> {
                offset = l1BlockBits + l1SetIndexBitCount;
                tag = Integer.parseInt(binaryAddress.substring(0, binaryAddress.length() - offset), 2);
                blockOffset = Integer.parseInt(binaryAddress.substring(binaryAddress.length() - l1BlockBits), 2);
                if (l1SetIndexBitCount == 0)
                    return new TraceDTO(l1DataCache.get(0), tag, blockOffset);
                offset = l1BlockBits + l1SetIndexBitCount;
                set = binaryAddress.substring(binaryAddress.length() - offset,
                        binaryAddress.length() - l1BlockBits + 1);
                return new TraceDTO(l1DataCache.get(Integer.parseInt(set, 2)), tag, blockOffset);
            }
            case L2 -> {
                offset = l2BlockBits + l2SetIndexBitCount;
                tag = Integer.parseInt(binaryAddress.substring(0, binaryAddress.length() - offset), 2);
                blockOffset = Integer.parseInt(binaryAddress.substring(binaryAddress.length() - l2BlockBits), 2);
                if (l2SetIndexBitCount == 0)
                    return new TraceDTO(l2Cache.get(0), tag, blockOffset);
                set = binaryAddress.substring(binaryAddress.length() - offset,
                        binaryAddress.length() - l2BlockBits + 1);
                return new TraceDTO(l2Cache.get(Integer.parseInt(set, 2)), tag, blockOffset);
            }
            default -> {
                System.err.println("Invalid cache type");
                System.exit(-1);
                return null;
            }
        }
    }
    // End


    // Start RAM related functions
    private static void initRam(String filename) {
        Path filepath = FileSystems.getDefault().getPath(filename);
        if (!Files.exists(filepath)) {
            System.err.printf("File not found: %s\n", filename);
        }
        try {
            ram = Files.readAllBytes(filepath);
        } catch (IOException ex) {
            System.err.println("IO error while reading RAM.");
            ex.printStackTrace();
            System.exit(ex.hashCode());
        }
    }
    // End

    // Start argument init functions
    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            setArg(args[i], args[i + 1]);
        }

        l1DataCache = new ArrayList<>(getL1SetCount());
        for (int i = 0; i < getL1SetCount(); i++) { // init L1 instruction cache
            Set set = new Set(l1LinesPerSet);
            for (int j = 0; j < l1LinesPerSet; j++) {
                set.lines.add(j, new Line(getL1BlockSize()));
            }
            l1DataCache.add(i, set);
        }

        l1InstructionCache = new ArrayList<>(getL1SetCount());
        for (int i = 0; i < getL1SetCount(); i++) { // init L1 data cache
            Set set = new Set(l1LinesPerSet);
            for (int j = 0; j < l1LinesPerSet; j++) {
                set.lines.add(j, new Line(getL1BlockSize()));
            }
            l1InstructionCache.add(i, set);
        }

        l2Cache = new ArrayList<>(getL2SetCount());
        for (int i = 0; i < getL2SetCount(); i++) { // init L2 cache
            Set set = new Set(l2LinesPerSet);
            for (int j = 0; j < l2LinesPerSet; j++) {
                set.lines.add(j, new Line(getL2BlockSize()));
            }
            l2Cache.add(i, set);
        }
    }

    private static void setArg(String arg, String sValue) {
        int value = -1;
        try {
            value = Integer.parseInt(sValue);
        } catch (NumberFormatException ignored) {
            if (!arg.equals("-t")) { // A non-numeric value was entered for a numeric-only argument
                System.err.printf("The value of %s can only be an integer.\n", arg);
                System.exit(-1);
            }
        }

        switch (arg) {
            case "-L1s" -> l1SetIndexBitCount = value;
            case "-L1E" -> l1LinesPerSet = value;
            case "-L1b" -> l1BlockBits = value;
            case "-L2s" -> l2SetIndexBitCount = value;
            case "-L2E" -> l2LinesPerSet = value;
            case "-L2b" -> l2BlockBits = value;
            case "-t" -> traceFilename = sValue;
            default -> {
                System.err.printf("Cannot set %s more than once.\n", arg);
                System.exit(-1);
            }
        }
    }
    // End
}
