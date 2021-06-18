import java.util.ArrayList;
import java.util.List;

public class Set {
    public List<Line> lines;
    public int lastUpdatedLineIndex;

    public Set(int initialCapacity) {
        this.lines = new ArrayList<>(initialCapacity);
        this.lastUpdatedLineIndex = -1;
    }

    public void write(byte[] data,int tag,CacheType type) {
        for(int i=0;i<lines.size();i++){

            Line line = lines.get(i);

            if(!line.valid){
                lastUpdatedLineIndex = i;
                line.data = data;
                line.valid = true;
                line.tag = tag;
                return;

            }
        }

        // Cache was full. FIFO

        Line lineToBeUpdated = lines.get(lastUpdatedLineIndex);
        lineToBeUpdated.valid = true;
        lineToBeUpdated.tag = tag;
        lineToBeUpdated.data = data;
        HitMissEvictionCounter.getInstance(type).increaseEviction();


    }
}
