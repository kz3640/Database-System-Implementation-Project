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

    // creates a new page and returns the newly created page
    public Page createNewPage() throws FileNotFoundException, IOException {
        int newPageIndex = this.getTotalPages();
        Page newPage = new Page(newPageIndex, new ArrayList<>(), this.getSchema());
        this.pageBuffer.add(newPage);
        this.currentBufferSize += 1;

        if (this.maxBufferSize <= this.currentBufferSize) {
            writeLRUPage();
        }
        return newPage;
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

    // write the least recently used page to the db and remove it from the buffer
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
        this.writer.writePage(removedPage);
        this.currentBufferSize -= 1;
    }

    // empty buffer
    public void clearBuffer() throws IOException {
        while (this.currentBufferSize != 0) {
            writeLRUPage();
        }
    }

    // inserts a new page at an index
    public Page insertNewPage(int pageIndex) throws IOException {
        Page newPage = new Page(pageIndex, new ArrayList<>(), getSchema());
        return newPage;
    }

    // write the schema to the catalog file
    public void writeSchemaToFile(ArrayList<Object> schema) throws IOException {
        this.writer.writeSchemaToFile(schema);
    }

    public Schema getSchema() {
        return this.reader.getSchema();
    }

    public void initDB() throws IOException {
        writer.initDB();
    }

    // calculate the ammount of pages that exist in the db. This can be pages stored in the db or the buffer
    public int getTotalPages() throws IOException {
        int highestPageInBuffer = 0;

        for (Page page : this.pageBuffer) {
            if (page.getPageID() + 1 > highestPageInBuffer) {
                highestPageInBuffer = page.getPageID() + 1;
            }
        }

        int pagesInDb = reader.getTotalPages();
        return highestPageInBuffer >= pagesInDb ? highestPageInBuffer : pagesInDb;
    }

    // debugging
    public void printBuffer() {
        System.out.println("PAGE BUFFER");
        for (Page page : pageBuffer) {
            page.printPage();
        }
    }

    // debugging
    public void printDB() throws IOException {
        reader.printDB();
    }
}
