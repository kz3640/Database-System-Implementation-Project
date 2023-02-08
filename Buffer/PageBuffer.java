package Buffer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import Buffer.Page;
import IO.BinaryWriter;
import IO.BinaryReader;
import Schema.Schema;

public class PageBuffer {

    public ArrayList<Page> pageBuffer;
    public int maxBufferSize; // how many pages there are in the db
    public int currentBufferSize;
    public int pageSize;
    public BinaryReader reader;
    public BinaryWriter writer;

    public PageBuffer(int pageSize, int maxBufferSize, BinaryReader reader, BinaryWriter writer) {
        this.pageBuffer = new ArrayList<Page>();
        this.pageSize = pageSize;
        this.maxBufferSize = maxBufferSize;
        this.currentBufferSize = 0;
        this.reader = reader;
        this.writer = writer;
    }

    public Page createNewPage() throws FileNotFoundException, IOException {
        int newPageIndex = this.getTotalPages();
        Page newPage = new Page(newPageIndex, new ArrayList<>(), this.getSchema());
        writer.writePage(newPage);
        return this.getPage(newPageIndex);
    }

    /*
     * Itterates through each page and does a check to see if the page we're looking
     * for is in the buffer.
     * 
     * @param pageId A placeholder identifier until we decide on how we want to id
     * them.
     * 
     * @return A boolen representing if the page was found or not.
     */
    public Page getPage(int pageId) throws IOException {
        for (int i = 0; i < this.currentBufferSize; i++) {
            if (this.pageBuffer.get(i).getPageID() == pageId) {
                return this.pageBuffer.get(i);
            }
        }

        Page page = this.reader.getPage(pageId);
        if (page == null)
            return null;

        this.pageBuffer.add(page);
        this.currentBufferSize += 1;

        if (this.maxBufferSize <= this.currentBufferSize) {
            writeLRUPage();
        }

        return page;
    }

    public void addPageToBuffer(Page page) throws IOException {
        this.pageBuffer.add(page);
        this.currentBufferSize += 1;

        if (this.maxBufferSize < this.currentBufferSize) {
            writeLRUPage();
        }
    }

    public void writeLRUPage() throws IOException {
        Timestamp minTimestamp = pageBuffer.get(0).getTime();
        int idx = 0;
        for (int i = 1; i < this.currentBufferSize; i++) {
            if (this.pageBuffer.get(i).getTime().before(minTimestamp)) {
                idx = i;
                minTimestamp = this.pageBuffer.get(i).getTime();
            }
        }

        Page removedPage = this.pageBuffer.remove(idx);
        this.writer.writePage(removedPage); // replace this with ^ once BW is updated
        this.currentBufferSize -= 1;
    }

    public void clearBuffer() throws IOException {
        while (this.currentBufferSize != 0) {
            writeLRUPage();
        }
    }

    public Page insertNewPage(int pageIndex) throws IOException {
        // get new page from writer and shift bytes
        Page newPage = new Page(pageIndex, new ArrayList<>(), getSchema());
        writer.insertNewPage(newPage);
        updateBufferPagesID(pageIndex);
        return newPage;
    }

    public void updateBufferPagesID(int pageIndex) {
        for (Page page : pageBuffer) {
            if (page.getPageID() >= pageIndex) {
                page.incrementPageID();
            }
        }
    }

    public void writeSchemaToFile(ArrayList<Object> schema) throws IOException {
        this.writer.writeSchemaToFile(schema);
    }

    public Schema getSchema() {
        return this.reader.getSchema();
    }

    public void initDB() throws IOException {
        writer.initDB();
    }

    public int getTotalPages() throws IOException {
        return writer.getTotalPages();
    }

    public void printBuffer() {
        System.out.println("PAGE BUFFER");
        for (Page page : pageBuffer) {
            page.printPage();
        }
    }

    public void printDB() throws IOException {
        reader.printDB();
    }
}
