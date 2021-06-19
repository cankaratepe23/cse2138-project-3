public class TraceDTO {

    private final Set set;
    private final int tag;
    private final int blockOffset;

    public TraceDTO(Set set, int tag, int blockOffset) {
        this.set = set;
        this.tag = tag;
        this.blockOffset = blockOffset;
    }

    public Set getSet() {
        return set;
    }

    public int getTag() {
        return tag;
    }

    public int getBlockOffset() {
        return blockOffset;
    }
}
