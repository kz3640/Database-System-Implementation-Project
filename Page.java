import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import Schema.Schema;


public class Page {
    
    // This is what the PageBuffer needs
    public int page_id;
    public int size;
    public Timestamp timestamp;

    // This is what the BinaryWriter needs
    ArrayList<ArrayList<Object>> allRecords;
    String fileName;
    Schema schema;
    int pageNumber;
    int pageSize;

    public Page() {
        this.timestamp = Timestamp.from(Instant.now());
    }

    public Page(int page_id, int table_id, int size) {
        this.page_id = page_id;
        this.size = size;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public int getPageID() {
        return this.page_id;
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
