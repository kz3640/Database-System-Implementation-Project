package Buffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import Schema.Schema;
import Record.Record;
import Util.Util;

public class Page {
    ArrayList<Record> records;

    // This is what the PageBuffer needs
    private int pageId;
    private Schema schema;
    public Timestamp timestamp;

    public Page(int pageId, ArrayList<Record> records, Schema schema) {
        this.records = records;
        this.schema = schema;
        this.pageId = pageId;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
        this.timestamp = Timestamp.from(Instant.now());

    }

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

    public int getFreeSpace() {
        return Util.calculateJunkSpaceSize(this, this.schema.getPageSize());
    }

    public boolean canRecordFitInPage(Record record) {
        return getFreeSpace() - record.calculateRecordSize() >= 0;
    }

    public void printPage() {
        int indexOfRecord = 0;
        System.out.println();
        System.out.print("pageID: " + this.pageId);
        if (this.records.size() == 0) {
            System.out.println("\n page contains no records");
        }
        for (Record record : this.records) {
            System.out.print("\n record: " + indexOfRecord);
            record.printRecord();
            indexOfRecord++;
        }
        System.out.print("\n");
    }
}
