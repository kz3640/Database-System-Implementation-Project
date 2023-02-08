import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import Schema.Schema;

public class Page {
    ArrayList<ArrayList<Object>> records;

    // This is what the PageBuffer needs
    private int pageId;
    private Schema schema;
    public Timestamp timestamp;

    public Page(int pageId, ArrayList<ArrayList<Object>> records, Schema schema) {
        this.records = records;
        this.schema = schema;
        this.pageId = pageId;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public void setRecords(ArrayList<ArrayList<Object>> records) {
        this.records = records;
        this.timestamp = Timestamp.from(Instant.now());

    }

    public void addRecord(int indexToBeAdded, ArrayList<Object> data) {
        this.records.add(indexToBeAdded, data);
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

    public ArrayList<ArrayList<Object>> getRecords() {
        return records;
    }

    public void incrementPageID() {
        this.pageId++;
    }

    public int getFreeSpace() {
        return Util.calculateJunkSpaceSize(records, this.schema.getPageSize());
    }

    public boolean canRecordFitInPage(ArrayList<Object> record) {
        return getFreeSpace() - Util.calculateRecordSize(record) >= 0;
    }

    public void printPage() {
        int indexOfRecord = 0;
        System.out.println();
        System.out.print("pageID: " + this.pageId);
        if (this.records.size() == 0) {
            System.out.println("\n page contains no records");
        }
        for (ArrayList<Object> record : this.records) {
            System.out.print("\n record: " + indexOfRecord);
            for (Object col : record) {
                System.out.print(" | ");
                System.out.print(col);
            }
            indexOfRecord++;
        }
        System.out.print("\n");
    }
}
