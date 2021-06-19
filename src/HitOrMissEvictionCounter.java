public class HitOrMissEvictionCounter {
    private int miss;
    private int hit;
    private int eviction;

    private static HitOrMissEvictionCounter l1Iinstance;
    private static HitOrMissEvictionCounter l1Dinstance;
    private static HitOrMissEvictionCounter l2instance;


    public static HitOrMissEvictionCounter getInstance(CacheType type) {

        switch (type) {
            case L1D:
                if (l1Dinstance == null) {
                    l1Dinstance = new HitOrMissEvictionCounter();
                }
                return l1Dinstance;

            case L1I:
                if (l1Iinstance == null) {
                    l1Iinstance = new HitOrMissEvictionCounter();
                }
                return l1Iinstance;

            case L2:
                if (l2instance == null) {
                    l2instance = new HitOrMissEvictionCounter();
                }
                return l2instance;
        }


        return l1Dinstance;
    }

    private HitOrMissEvictionCounter() {
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
