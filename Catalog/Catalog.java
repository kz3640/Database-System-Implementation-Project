package Catalog;

import java.util.HashMap;
import java.util.Map;

import Record.Record;
import StorageManager.StorageManager;

public class Catalog {
    private String path;
    private int pageSize;
    private int bufferSize;
    private Map<String, Schema> tables;

    public Catalog(String path, int pageSize, int bufferSize) {
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.path = path;
        this.tables = new HashMap<>();
    }

    public Map<String, Schema> getTables() {
        return tables;
    }

    public boolean doesTableNameExist(String tableName) {
        return this.tables.get(tableName) != null;
    }

    public void addSchema(Schema schema) {
        tables.put(schema.getTableName(), schema);
    }

    public Schema getSchemaByName(String tableName) {
        Schema schema = this.tables.get(tableName);
        if (schema == null) {
            System.out.println("---ERROR---");
            System.out.println("Table name " + tableName + " not found.\n");
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

    public void dropTable(Schema schema) {
        this.tables.remove(schema.getTableName());
    }

    public void balanceTableIndex(int indexRemoved) {
        for (Schema schema : this.tables.values()) {
            int schemaIndex = Integer.parseInt(schema.getIndex());
            if ( schemaIndex >  indexRemoved) {
                schema.setIndex(String.valueOf(schemaIndex - 1));
            }
        }
    }

    public void printCatalog(StorageManager sm) {
        System.out.println();
        System.out.println("DB Location: " + path);
        System.out.println("Page Size: " + this.pageSize);
        System.out.println("Buffer Size: " + this.bufferSize);
        System.out.println("Tables: " + this.tables.size());

        for (Schema schema : this.tables.values()) {
            System.out.println();
            sm.printTableInfo(schema.getTableName());
            System.out.println();
        }

        System.out.println("\nSUCCESS!");
    }
}
