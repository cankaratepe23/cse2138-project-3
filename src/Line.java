public class Line {
    public boolean valid;
    public int tag;
    public byte[] data;
    public int age;

    // tag???
    public Line(int blocksize) {
        data = new byte[blocksize];
    }

    public void incrementAge() {
        age++;
    }
}
