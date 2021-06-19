public class HitMissEvictionCounter {
    private int miss;
    private int hit;
    private int eviction;

    private static HitMissEvictionCounter l1Iinstance;
    private static HitMissEvictionCounter l1Dinstance;
    private static HitMissEvictionCounter l2instance;


    public static HitMissEvictionCounter getInstance(CacheType type) {

        switch (type) {
            case L1D:
                if (l1Dinstance == null) {
                    l1Dinstance = new HitMissEvictionCounter();
                }
                return l1Dinstance;

            case L1I:
                if (l1Iinstance == null) {
                    l1Iinstance = new HitMissEvictionCounter();
                }
                return l1Iinstance;

            case L2:
                if (l2instance == null) {
                    l2instance = new HitMissEvictionCounter();
                }
                return l2instance;
        }


        return l1Dinstance;
    }

    private HitMissEvictionCounter() {
    }

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
