import java.util.Arrays;

public class Line {
    public boolean valid;
    public int tag;
    public byte[] data;
    public int age;

    public Line(int blockSize) {
        data = new byte[blockSize];
    }

    public void incrementAge() {
        age++;
    }

    @Override
    public String toString() {
        return "Line{" +
                "valid=" + valid +
                ", tag=" + tag +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
