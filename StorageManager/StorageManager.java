package StorageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Buffer.PageBuffer;
import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Catalog.Catalog;
import Catalog.Schema;

public class StorageManager {
    private Catalog catalog;
    public PageBuffer pageBuffer;

    public StorageManager(PageBuffer pageBuffer, Catalog catalog) {
        this.pageBuffer = pageBuffer;
        this.catalog = catalog;
        return;
    }

    // return the current catalog. This is for any class that doesn't have access to
    // the catalog
    public Catalog getCatalog() {
        return catalog;
    }

    public boolean addSchema(Schema schema) {
        if (catalog.doesTableNameExist(schema.getTableName())) {
            System.out.println("Table with name " + schema.getTableName() + " already exists.");
            return false;
        }
        pageBuffer.writer.addSchemaToFile(schema);
        this.catalog.addSchema(schema);
        return true;
    }

    // healper for creating the catalog file.
    public ArrayList<Object> addDataToArray(String[] input) {
        ArrayList<Object> data = new ArrayList<Object>();
        for (String s : input) {
            if (s.equals("null")) {
                data.add(null);
                continue;
            }
            try {
                int i = Integer.parseInt(s);
                data.add(i);
            } catch (NumberFormatException e) {
                String lowerString = s.toLowerCase();
                if (lowerString.equals("true") || lowerString.equals("false")) {
                    boolean b = Boolean.parseBoolean(s);
                    data.add(b);
                } else {
                    try {
                        double d = Double.parseDouble(s);
                        data.add(d);
                    } catch (NumberFormatException exx) {
                        if (s.length() == 1) {
                            data.add(s.charAt(0));
                        } else {
                            data.add(s);
                        }
                    }
                }
            }
        }
        return data;
    }

    // add record into page.
    // return: false -> record doesn't go into page
    // true -> record was added into page
    public boolean insertRecordInPage(Page page, Record record, Schema schema, boolean lastPage) {
        boolean shouldBeAdded = false;
        int indexToBeAdded = 0;

        ArrayList<Record> pageRecords = page.getRecords();
        if (pageRecords.size() == 0) {
            pageRecords.add(record);
            return true;
        }

        int indexOfPrimaryKey = schema.getIndexOfPrimaryKey();

        // compare primary key and insert if possible
        for (int i = 0; i < pageRecords.size(); i++) {
            Record recordInPage = pageRecords.get(i);
            RecordAttribute primaryKeyRecord = recordInPage.getData().get(indexOfPrimaryKey);
            RecordAttribute primaryKeyData = record.getData().get(indexOfPrimaryKey);
            if (primaryKeyData.getType() == int.class) {
                if ((Integer) primaryKeyRecord.getAttribute() > (Integer) primaryKeyData.getAttribute()) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData.getType() == boolean.class) {
                if (!((Boolean) primaryKeyRecord.getAttribute()) && ((Boolean) primaryKeyData.getAttribute())) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData.getType() == Character.class || primaryKeyData.getType() == String.class) {
                String s1 = (String) primaryKeyRecord.getAttribute();
                String s2 = (String) primaryKeyData.getAttribute();
                if (s1.compareTo(s2) > 0) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData.getType() == Double.class) {
                if ((Double) primaryKeyRecord.getAttribute() > (Double) primaryKeyData.getAttribute()) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            }
        }

        if (lastPage && !shouldBeAdded) {
            shouldBeAdded = true;
            indexToBeAdded = pageRecords.size();
        }

        // couldn't find a spot for the primary key so return false
        if (!shouldBeAdded) {
            return false;
        }

        // check remaining space and see if we need to split
        if (page.canRecordFitInPage(record)) {
            page.addRecord(indexToBeAdded, record);
        } else {
            page.addRecord(indexToBeAdded, record);
            splitPage(page);
        }

        return true;
    }

    // split page and add record to correct page
    public void splitPage(Page page) {

        // we need to update the id of every other page so we loop over all of them and
        // increment the pageID
        int pageIndex = this.pageBuffer.getTotalPages(page.getSchema()) - 1;

        while (true) {
            if (pageIndex <= page.getPageID())
                break;
            Page pageToUpdate = this.pageBuffer.getPage(pageIndex, page.getSchema());
            pageToUpdate.incrementPageID();
            pageIndex--;
        }

        Page newPage = pageBuffer.insertNewPage(page.getPageID() + 1, page.getFileName());

        // split records evenly
        ArrayList<Record> records = page.getRecords();
        int splitIndex = records.size() / 2;
        ArrayList<Record> firstHalf = new ArrayList<>(records.subList(0, splitIndex));
        ArrayList<Record> secondHalf = new ArrayList<>(records.subList(splitIndex, records.size()));

        page.setRecords(firstHalf);
        newPage.setRecords(secondHalf);
        pageBuffer.addPageToBuffer(newPage);
    }

    public void select(String args, String tableName) {
        this.catalog.getSchemaByName(tableName);
        Schema schema =  this.catalog.getSchemaByName(tableName);

        schema.printSchema();

        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        while (true) {
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema);

            page.printPage();

            pageIndex++;
        }
    }

    // add record to db
    public void addRecord(Record record) {
        // create new file if doesn't exist
        File db = new File(this.catalog.getFileOfRecord(record));
        try {
            db.createNewFile();
        } catch (IOException e) {
        }

        Schema schema = this.catalog.getSchemaByName(record.getTableName());
        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        while (true) {
            if (pagesInTable <= pageIndex)
                pageBuffer.createNewPage(schema);

            Page page = this.pageBuffer.getPage(pageIndex, schema);

            if (insertRecordInPage(page, record, schema, pagesInTable - 1 == pageIndex))
                break;

            pageIndex++;
        }
    }

    public boolean isPrimaryKeyUsed(Record record) {
        Schema schema = this.catalog.getSchemaByName(record.getTableName());
        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        while (true) {
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema);

            if (page.isPrimaryKeyInPage(record))
                return true;

            pageIndex++;
        }

        return false;
    }

    public boolean printTableInfo(String tableName) {
        System.out.println();
        Schema schema = this.catalog.getSchemaByName(tableName);
        if (schema == null) {
            return false;
        }
        schema.printSchema();

        System.out.println("Pages: " + pageBuffer.getTotalPages(schema));
        System.out.println("Records: "+ pageBuffer.getRecordAmmount(schema, tableName));

        return true;
    }

    // empty buffer
    public void writeBuffer() throws IOException {
        pageBuffer.clearBuffer();
    }

    public void printBuffer() {
        pageBuffer.printBuffer();
    }
}
