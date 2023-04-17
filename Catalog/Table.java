package Catalog;

import Record.Record;
import java.util.ArrayList;

public class Table {
    private ArrayList<Record> records;
    private String tableName;
    private Schema schema;

    public Table(ArrayList<Record> recordsInTable, String tableName, Schema schema) {
        this.records = recordsInTable;
        this.tableName = tableName;
        this.schema = schema;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public Schema getSchema() {
        return schema;
    }
    
    public String getTableName() {
        return tableName;
    }
}
