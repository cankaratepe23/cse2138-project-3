import java.util.ArrayList;
import java.util.List;

public class Set {
    public final List<Line> lines;

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

    public void write(byte[] data, int tag, CacheType type) {
        for (Line line : lines) {
            if (line.valid)
                line.incrementAge();
        }

        for (Line line : lines) {
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
        HitOrMissEvictionCounter.getInstance(type).increaseEviction();


    }

    @Override
    public String toString() {
        return "Set" + lines;
    }
}
