import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    ///L1s
    private static int l1SetIndexBitCount;
    ///L1 - Number of sets
    private static int getL1SetCount() { return (int) Math.pow(2, l1SetIndexBitCount); }
    ///L1E
    private static int l1LinesPerSet;
    ///L1b
    private static int l1BlockBits;
    ///L1 - Size of block (Number of bytes in block)
    private static int getL1BlockSize() { return (int) Math.pow(2, l1BlockBits); }
    ///L2s
    private static int l2SetIndexBitCount;
    ///L2 - Number of sets
    private static int getL2SetCount() { return (int) Math.pow(2, l2SetIndexBitCount); }
    ///L2E
    private static int l2LinesPerSet;
    ///L2b
    private static int l2BlockBits;
    ///L2 - Size of block (Number of bytes in block)
    private static int getL2BlockSize() { return (int) Math.pow(2, l2BlockBits); }
    private static String traceFilename;
    private static byte[] ram;

    public static void main(String[] args) {
        parseArguments(args);
        initRam("RAM.dat");
        readTrace("traces/test.trace");
        System.out.println("Annen");
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
            System.out.println(line);
            parseTraceLine(line);
        }
    }

    private static void parseTraceLine(String line) {
        char operation = line.charAt(0);
        String sAddress = line.substring(2, 9);
        if (operation == 'I' || operation == 'L') { // format is: op address, size
            String sSize = line.substring(line.indexOf(',') + 2);
            executeOperation(operation, sAddress, sSize);
        }
        else if (operation == 'M' || operation == 'S') { // format is: op address, size, data
            String sSize = line.substring(line.indexOf(',') + 2, line.lastIndexOf(','));
            String sData = line.substring(line.lastIndexOf(',') + 2);
            executeOperation(operation, sAddress, sSize, sData);
        }
        else {
            System.err.printf("Invalid data found in trace:\n%s", line);
            System.exit(-1);
        }
    }
    // End


    // Start operation logic
    private static void executeOperation(char operation, String sAddress, String sSize) {
        if (operation == 'I') {
            loadInstruction(sAddress, sSize);
        }
        else if (operation == 'L') {
            loadData(sAddress, sSize);
        }
    }

    private static void executeOperation(char operation, String sAddress, String sSize, String sData) {
        if (operation == 'S') {
            storeData(sAddress, sSize, sData);
        }
        else if (operation == 'M') {
            modifyData(sAddress, sSize, sData);
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
        }
        catch (IOException ex) {
            System.err.println("IO error while reading RAM.");
            ex.printStackTrace();
            System.exit(ex.hashCode());
        }
    }
    // End

    // Start argument init functions
    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i+=2) {
            setArg(args[i], args[i + 1]);
        }
    }

    private static void setArg(String arg, String sValue) {
        int value = -1;
        try {
            value = Integer.parseInt(sValue);
        }
        catch (NumberFormatException ignored) {
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
