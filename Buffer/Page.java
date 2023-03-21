package Buffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import Catalog.Catalog;
import Catalog.Schema;
import Record.Record;
import Record.RecordAttribute;
import Util.Util;

public class Page {
    ArrayList<Record> records;

    private String fileName;
    private int pageId;
    private Catalog catalog;
    private Timestamp timestamp;

    public Page(int pageId, ArrayList<Record> records, Catalog catalog, String fileName) {
        this.records = records;
        this.fileName = fileName;
        this.catalog = catalog;
        this.pageId = pageId;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
        this.timestamp = Timestamp.from(Instant.now());

    }

    // adds record to record list
    public void addRecord(int indexToBeAdded, Record record) {
        this.records.add(indexToBeAdded, record);
        this.timestamp = Timestamp.from(Instant.now());
    }

    public int getPageID() {
        return this.pageId;
    }

    public void setTime() {
        this.timestamp = Timestamp.from(Instant.now());
    }

    public Timestamp getTime() {
        return this.timestamp;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public void incrementPageID() {
        this.pageId++;
    }

    public void decrementPageID() {
        this.pageId--;
    }

    // calculate how much free space exists after all of the records
    public int getFreeSpace() {
        return Util.calculateJunkSpaceSize(this, this.catalog.getPageSize());
    }

    // can record fit into the page with the given page size
    public boolean canRecordFitInPage(Record record) {
        return getFreeSpace() - record.calculateRecordSize() >= 0;
    }

    public String getFileName() {
        return fileName;
    }

    public Schema getSchema() {
        String tableName = this.catalog.fileNameToTableName(this.fileName);
        return this.catalog.getSchemaByName(tableName);
    }

    public boolean isPrimaryKeyInPage(Record record) {
        Schema schema = getSchema();
        for (Record recordInPage : records) {
            Object primAttr = recordInPage.getData().get(schema.getIndexOfPrimaryKey()).getAttribute();
            Object primRecordAttr = record.getData().get(schema.getIndexOfPrimaryKey()).getAttribute();
            if (primAttr.equals(primRecordAttr))
                return true;
        }
        return false;
    }

    public boolean isUniqueValueUnique(Record record) {
        Schema schema = getSchema();

        ArrayList<Integer> indexesOfUniqueValues = schema.getIndexesOfUniqueValues();
        for (Record recordInPage : records) {
            for (Integer integer : indexesOfUniqueValues) {
                Object primAttr = recordInPage.getData().get(integer).getAttribute();
                Object primRecordAttr = record.getData().get(integer).getAttribute();
                if (primAttr == null || primRecordAttr == null) {
                    continue;
                }
                if (primAttr.equals(primRecordAttr))
                    return false;
            }
        }
        return true;
    }

    public void printPage() {
        int indexOfRecord = 0;
        // System.out.println("pageID: " + this.pageId); // debugging
        if (this.records.size() == 0) {
            System.out.println("\n page contains no records");
        }
        for (Record record : this.records) {
            // System.out.print("\n record " + indexOfRecord + ": ");
            record.printRecord();
            System.out.println("");
            indexOfRecord++;
        }
    }
}
