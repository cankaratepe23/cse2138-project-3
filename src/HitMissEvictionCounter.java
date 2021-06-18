public class HitMissEvictionCounter {
    private int miss;
    private int hit;
    private int eviction;

    public int getMiss() {
        return miss;
    }

    public void increaseMiss() {
        this.miss++;
    }

    public int getHit() {
        return hit;
    }

    public void increaseHit() {
        this.hit++;
    }

    public int getEviction() {
        return eviction;
    }

    public void increaseEviction() {
        this.eviction++;
    }
}
