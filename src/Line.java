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
}
