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
import Catalog.SchemaAttribute;
import InputHandler.BooleanExpressionEvaluator;

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
            System.out.println("---ERROR---");
            System.out.println("Table with name " + schema.getTableName() + " already exists.\n");
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
            Page pageToUpdate = this.pageBuffer.getPage(pageIndex, page.getSchema(), true);
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
        Schema schema = this.catalog.getSchemaByName(tableName);

        schema.printSchema();

        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        System.out.println("");
        while (true) {
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            page.printPage();

            pageIndex++;
        }
        System.out.println("");
    }

    // add record to db
    public void addRecord(Record record, Schema schema) {
        // create new file if doesn't exist
        File db = new File(this.catalog.getFileOfRecord(record));
        try {
            db.createNewFile();
        } catch (IOException e) {
        }

        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        while (true) {
            if (pagesInTable <= pageIndex)
                pageBuffer.createNewPage(schema);

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            if (insertRecordInPage(page, record, schema, pagesInTable - 1 == pageIndex))
                break;

            pageIndex++;
        }
    }

    public boolean doesRecordFollowConstraints(Record record, String tableName) {
        Schema schema = this.catalog.getSchemaByName(record.getTableName());
        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);
        while (true) {
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            if (page.isPrimaryKeyInPage(record)) {
                System.out.println("---ERROR---");
                System.out.println("Primary key is already in use\n");
                return true;
            }

            if (!page.isUniqueValueUnique(record)) {
                System.out.println("---ERROR---");
                System.out.println("Value is not unique\n");
                return true;
            }

            pageIndex++;
        }

        return false;
    }

    public boolean dropTable(Schema schema) {
        pageBuffer.removeSchemaFromBuffer(schema);
        pageBuffer.writer.deleteFile(schema);
        pageBuffer.writer.removeTableFromCatalog(schema);
        return true;
    }

    private Object getDefaultValue(String defaultString, String typeAsString) {

        return null;
    }

    private Record convertRecord(Schema oldSchema, Schema newSchema, Record record) {
        ArrayList<RecordAttribute> recordAttributes = record.getData();

        if (oldSchema.getAttributes().size() > newSchema.getAttributes().size()) {
            ArrayList<String> newAttrs = new ArrayList<>();
            for (SchemaAttribute newSchemaAttribute : newSchema.getAttributes()) {
                newAttrs.add(newSchemaAttribute.getAttributeName());
            }

            // add new attribute to record
            for (SchemaAttribute oldSchemaAttribute : oldSchema.getAttributes()) {
                String oldAttrName = oldSchemaAttribute.getAttributeName();
                if (!newAttrs.contains(oldAttrName)) {
                    recordAttributes.remove(oldSchema.getAttributes().indexOf(oldSchemaAttribute));
                }
            }

        } else {
            ArrayList<String> oldAttrs = new ArrayList<>();
            for (SchemaAttribute oldSchemaAttribute : oldSchema.getAttributes()) {
                oldAttrs.add(oldSchemaAttribute.getAttributeName());
            }

            // add new attribute to record
            for (SchemaAttribute newSchemaAttribute : newSchema.getAttributes()) {
                String newAttrName = newSchemaAttribute.getAttributeName();

                if (!oldAttrs.contains(newAttrName)) {
                    RecordAttribute newAttribute;
                    if (newSchemaAttribute.getDefault() == null) {
                        newAttribute = new RecordAttribute(null, null, newSchemaAttribute.getLength());
                    } else {
                        newAttribute = new RecordAttribute(newSchemaAttribute.getType(),
                                newSchemaAttribute.getDefault(), newSchemaAttribute.getLength());
                    }
                    recordAttributes.add(newAttribute);
                }
            }
        }

        return new Record(recordAttributes, newSchema.getTableName());
    }

    public boolean alterSchema(Schema oldSchema, Schema newSchema) {
        pageBuffer.clearBuffer();
        oldSchema.convertToTemp();
        pageBuffer.writer.initDB(newSchema);

        // loop over every page
        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(oldSchema);
        while (true) {
            if (pagesInTable <= pageIndex) // done
                break;

            Page page = this.pageBuffer.getPage(pageIndex, oldSchema, false);
            ArrayList<Record> convertedRecords = new ArrayList<>();
            for (Record record : page.getRecords()) {
                Record newRecord = convertRecord(oldSchema, newSchema, record);
                convertedRecords.add(newRecord);
                newRecord.printRecord();
            }
            for (Record record : convertedRecords) {
                addRecord(record, newSchema);
            }
            pageIndex++;
        }

        oldSchema.remove();
        this.catalog.addSchema(newSchema);
        this.pageBuffer.writer.alterTable(newSchema);
        return true;
    }

    public boolean alterDropSchema(Schema oldSchema, Schema newSchema) {
        return true;
    }

    public boolean printTableInfo(String tableName) {
        System.out.println();
        Schema schema = this.catalog.getSchemaByName(tableName);
        if (schema == null) {
            return false;
        }
        schema.printSchema();

        // System.out.println("Index: " + schema.getIndex()); debugging
        System.out.println("Pages: " + pageBuffer.getTotalPages(schema));
        System.out.println("Records: " + pageBuffer.getRecordAmmount(schema, tableName));

        return true;
    }

    public void delete(String tableName, String logic) {
        // boolean result = BooleanExpressionEvaluator.evaluate(logic);

        this.catalog.getSchemaByName(tableName);
        Schema schema = this.catalog.getSchemaByName(tableName);

        int pageIndex = 0;

        while (true) {
            int pagesInTable = this.pageBuffer.getTotalPages(schema);
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            ArrayList<Record> newRecords = new ArrayList<>();
            for (Record record : page.getRecords()) {
                if (!BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                    newRecords.add(record);
                }
            }

            if (newRecords.size() == 0) {
                pageBuffer.removePage(page);
                removePage(page);
                continue;
            } else if (newRecords.size() != page.getRecords().size()) {
                page.setRecords(newRecords);
            }

            pageIndex++;
        }

        // this.pageBuffer.updatePageTotal(schema, pagesLeft);
        System.out.println("");
    }

    public void removePage(Page page) {
        int lastPage = this.pageBuffer.getTotalPages(page.getSchema()) - 1;

        int pageIndex = page.getPageID() + 1;

        while (true) {
            if (pageIndex > lastPage)
                break;
            Page pageToUpdate = this.pageBuffer.getPage(pageIndex, page.getSchema(), true);
            pageToUpdate.decrementPageID();

            if (pageIndex == lastPage) {
                this.pageBuffer.updatePageTotal(pageToUpdate.getSchema(), pageToUpdate.getPageID());
            }
            pageIndex++;
        }
    }

    public void update(String tableName, String col, String val, String logic) {
        this.catalog.getSchemaByName(tableName);
        Schema schema = this.catalog.getSchemaByName(tableName);

        int pageIndex = 0;

        while (true) {
            int pagesInTable = this.pageBuffer.getTotalPages(schema);
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            for (Record record : page.getRecords()) {
                if (!BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                    // needed for logic
                    int indexOfPrimaryKey = schema.getIndexOfPrimaryKey();
                    String nameOfPrimaryAttribute = schema.getAttributes().get(indexOfPrimaryKey).getAttributeName();
                    RecordAttribute recordAttribute = record.getData().get(schema.getIndexOfPrimaryKey());
                    String valueOfPrimaryString = recordAttribute.getAttribute().toString();

                    // new logic for deleting
                    String logicString =  nameOfPrimaryAttribute + " = " + valueOfPrimaryString;
                    delete(tableName, logicString);

                    // update record
                }
            }

            // if (newRecords.size() == 0) {
            //     pageBuffer.removePage(page);
            //     removePage(page);
            //     continue;
            // } else if (newRecords.size() != page.getRecords().size()) {
            //     page.setRecords(newRecords);
            // }

            pageIndex++;
        }

        // this.pageBuffer.updatePageTotal(schema, pagesLeft);
        System.out.println("");

    }

    // empty buffer
    public void writeBuffer() throws IOException {
        pageBuffer.clearBuffer();
    }

    public void printBuffer() {
        pageBuffer.printBuffer();
    }

}
