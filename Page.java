import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;


public class Page {
    
    public int page_id;
    public int table_id;
    public int size;
    public Timestamp timestamp;

    public Page(int page_id, int table_id, int size) {
        this.page_id = page_id;
        this.table_id = table_id;
        this.size = size;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public int getPageID() {
        return this.page_id;
    }

    public int getTableID(){
        return this.table_id;
    }

    public int getSize(){
        return this.size;
    }

    public void setTime(){
        this.timestamp = Timestamp.from(Instant.now());
    }

    public Timestamp getTime(){
        return this.timestamp;
    }

}
