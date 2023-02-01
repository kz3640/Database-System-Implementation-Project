import java.util.ArrayList;

public class PageBuffer {
    
    private class Page {
        private int page_id;

        private Page(int page_size) {
            this.page_id = 0;
        }

        public int getID() {
            return this.page_id;
        }

    }

    public ArrayList<Page> page_buff;
    public int buffer_size;
    public int current_size;

    public PageBuffer(int page_size, int buffer_size) {
        this.page_buff = new ArrayList<Page>();
        this.buffer_size = buffer_size;
        this.current_size = 0;
    }


    /*
     * Itterates through each page and does a check to see if the page we're looking for is in the buffer.
     * @param page_id A placeholder identifier until we decide on how we want to id them.
     * @return A boolen representing if the page was found or not.
     */
    public Boolean isPageInBuffer(int page_id) {
        for(int i = 0; i < this.buffer_size; i++) {
            if(this.page_buff.get(i).getID() == page_id) {
                return true;
            }
        }
        return false;
    }


    /*
     * @return A Page object from the buffer.
     */
    public Page getPage() {
        return new Page(0);
    }


    /*
     * @return A boolean representing if the buffer has the maximum number of pages it can hold.
     */
    public Boolean isBufferFull() {
        return current_size == buffer_size;
    }

}
