package Buffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import Buffer.Page;
import IO.BinaryWriter;
import IO.BinaryReader;
import Catalog.Catalog;
import Catalog.Schema;

public class PageBuffer {

    public ArrayList<Page> pageBuffer;
    public int maxBufferSize; // how many pages there are in the db
    public int currentBufferSize;
    public BinaryReader reader;
    public BinaryWriter writer;
    public Catalog catalog;

    public PageBuffer(int maxBufferSize, BinaryReader reader, BinaryWriter writer, Catalog catalog) {
        this.pageBuffer = new ArrayList<Page>();
        this.maxBufferSize = maxBufferSize;
        this.currentBufferSize = 0;
        this.reader = reader;
        this.writer = writer;
        this.catalog = catalog;
    }

    // creates a new page and returns the newly created page
    public Page createNewPage(Schema schema) {
        int newPageIndex = this.getTotalPages(schema);
        Page newPage = new Page(newPageIndex, new ArrayList<>(), this.catalog, schema.getFileName());
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
    public Page getPage(int pageId, Schema schema) {
        for (int i = 0; i < this.currentBufferSize; i++) {
            Page page = this.pageBuffer.get(i);
            if (page.getPageID() == pageId && page.getFileName().equals(schema.getFileName())) {
                return page;
            }
        }

        Page page = this.reader.getPage(pageId, schema);
        if (page == null)
            return null;

        this.pageBuffer.add(page);
        this.currentBufferSize += 1;

        if (this.maxBufferSize <= this.currentBufferSize) {
            writeLRUPage();
        }

        return page;
    }

    public void addPageToBuffer(Page page) {
        this.pageBuffer.add(page);
        this.currentBufferSize += 1;

        if (this.maxBufferSize < this.currentBufferSize) {
            writeLRUPage();
        }
    }

    // write the least recently used page to the db and remove it from the buffer
    public void writeLRUPage() {
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
    public Page insertNewPage(int pageIndex, String fileName) {
        Page newPage = new Page(pageIndex, new ArrayList<>(), this.catalog, fileName);
        return newPage;
    }

    // calculate the ammount of pages that exist in the db. This can be pages stored in the db or the buffer
    public int getTotalPages(Schema schema) {
        String fileName = schema.getFileName();
        int highestPageInBuffer = 0;

        for (Page page : this.pageBuffer) {
            if (page.getPageID() + 1 > highestPageInBuffer && page.getFileName().equals(fileName)) {
                highestPageInBuffer = page.getPageID() + 1;
            }
        }

        int pagesInDb = reader.getTotalPages(fileName);
        return highestPageInBuffer >= pagesInDb ? highestPageInBuffer : pagesInDb;
    }

    public void printTableInfo(String tableName) {
        reader.printTableInfo(tableName);
    }

    public int getRecordAmmount(Schema schema) {
        return this.reader.getRecordAmmount(schema);
    }

    // debugging
    public void printBuffer() {
        System.out.println("PAGE BUFFER");
        for (Page page : pageBuffer) {
            page.printPage();
        }
    }

    // debugging
    // public void printDB() throws IOException {
    //     reader.printDB();
    // }
}
