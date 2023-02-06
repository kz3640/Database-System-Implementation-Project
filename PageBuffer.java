import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

public class PageBuffer {

    public ArrayList<Page> page_buff;
    public int max_page_count;
    public int current_page_count;
    public int page_size;
    public BinaryReader bReader;
    public BinaryWriter bWriter;

    public PageBuffer(int page_size, int max_page_count, BinaryReader bReader, BinaryWriter bWriter) {
        this.page_buff = new ArrayList<Page>();
        this.page_size = page_size;
        this.max_page_count = max_page_count;
        this.current_page_count = 0;
        this.bReader = bReader;
        this.bWriter = bWriter;
    }


    /*
     * Itterates through each page and does a check to see if the page we're looking for is in the buffer.
     * @param page_id A placeholder identifier until we decide on how we want to id them.
     * @return A boolen representing if the page was found or not.
     */
    public Page getPage(int page_id) throws IOException{
        for(int i = 0; i < this.current_page_count; i++) {
            if(this.page_buff.get(i).getPageID() == page_id) {
                return this.page_buff.get(i);
            }
        }

        if (this.max_page_count <= this.current_page_count){
            writeLRUPage();
        }
        this.current_page_count += 1;
        return new Page(); // read page from BR here

    }


    public void writeLRUPage()throws IOException{
        Timestamp min_timestamp = page_buff.get(0).getTime();
        int idx = 0;
        for(int i = 1; i < this.current_page_count; i++) {
            if(this.page_buff.get(i).getTime().before(min_timestamp)) {
                idx = i;
                min_timestamp = this.page_buff.get(i).getTime();
            }
        }
        this.bWriter.writePage(this.page_buff.get(idx).allRecords, this.page_buff.get(idx).fileName, 
                                this.page_buff.get(idx).schema, idx, this.page_size);
        this.current_page_count -= 1;
    }

    public void clearBuffer() throws IOException{
        while (this.current_page_count != 0){
            writeLRUPage();
        }
    }

    /*
     * @return A Page object from the buffer.
     */
    public Page getNewPage() {
        return new Page();
    }

}
