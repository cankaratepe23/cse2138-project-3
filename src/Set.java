import java.util.ArrayList;
import java.util.List;

public class Set {
    public List<Line> lines;
    public int oldestLineIndex;

    public Set(int initialCapacity) {
        this.lines = new ArrayList<>(initialCapacity);
    }

    private Line getOldest() {
        int max = Integer.MIN_VALUE;
        Line maxLine = null;
        for (Line line : lines) {
            if (line.age > max) {
                max = line.age;
                maxLine = line;
            }
        }
        return maxLine;
    }

    public void write(byte[] data, int tag, CacheType type, int blockOffset) {
        for (Line line : lines) {
            if (line.valid)
                line.incrementAge();
        }

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (!line.valid) {
                line.age = 0;
                line.data = data;
                line.valid = true;
                line.tag = tag;
                return;
            }
        }

        // Cache was full. FIRST OUT FIRST IN!!! AKA FOFI
        Line lineToBeUpdated = getOldest();
        lineToBeUpdated.valid = true;
        lineToBeUpdated.tag = tag;
        lineToBeUpdated.data = data;
        lineToBeUpdated.age = 0;
        HitMissEvictionCounter.getInstance(type).increaseEviction();


    }
}
