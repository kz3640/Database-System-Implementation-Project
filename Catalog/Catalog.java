package Catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Record.Record;

public class Catalog {
    private String path;
    private int pageSize;
    private int bufferSize;
    private Map<String, Schema> tables;
    // private ArrayList<SchemaAttribute> attributes;
    // private int indexOfPrimaryKey;

    public Catalog(String path, int pageSize, int bufferSize) {
        // this.tableName = tableName;
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        // this.attributes = attributes;
        this.path = path;

        this.tables = new HashMap<>();
    }

    public boolean doesTableNameExist(String tableName) {
        return this.tables.get(tableName) != null;
    }

    public void addSchema(Schema schema) {
        tables.put(schema.getTableName(), schema);
    }

    // public void setAttributes(ArrayList<SchemaAttribute> attributes) {
    // this.attributes = attributes;
    // }

    public Schema getSchemaByName(String tableName) {
        Schema schema = this.tables.get(tableName);
        if (schema == null) {
            System.out.println("Table name " + tableName + " not found.");
        }
        return schema;
    }

    public String getPath() {
        return path;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getFileOfRecord(Record record) {
        return this.path + this.getSchemaByName(record.getTableName()).getIndex() + "database.txt";
    }

    public String fileNameToTableName(String fileName) {
        String[] pathParts = fileName.split("/");
        String shortFileName = pathParts[pathParts.length - 1];
        String idString = shortFileName.replaceAll("[^\\d]", "");
        for (Schema schema : this.tables.values()) {
            if (schema.getIndex().equals(idString)) {
                return schema.getTableName();
            }
        }
        return null;
    }

    public void printCatalog() {
        System.out.println();
        System.out.println("DB Location: " + path);
        System.out.println("Page Size: " + this.pageSize);
        System.out.println("Buffer Size: " + this.bufferSize);
        System.out.println("Tables: " + this.tables.size());

        for (Schema schema : this.tables.values()) {
            System.out.println();
            schema.printSchema();
            System.out.println();
        }
    }
}
