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

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


    @Override
    public String toString() {
        return "Line{" +
                "valid=" + valid +
                ", tag=" + tag +
                ", data=" + byteArrayToHex(data) +
                '}';
    }
}
