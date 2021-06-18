public class Line {
    public boolean valid;
    public int tag;
    public byte[] data;
    // tag???
    public Line(int blocksize) {
        data = new byte[blocksize];
    }
}
