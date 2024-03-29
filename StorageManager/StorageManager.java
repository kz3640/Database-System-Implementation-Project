package StorageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import Buffer.PageBuffer;
import CartesianProduct.CartesianProduct;
import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Tree.BPlusTree;
import Tree.BPlusTree.PageInfo;
import Util.Util;
import Catalog.Catalog;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Catalog.Table;
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

    public void writeBPlusTrees() {
        Map<String, Schema> tables = this.catalog.getTables();
        for (Schema schema : tables.values()) {
            pageBuffer.writer.writeBPlusTree(schema);
        }
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
    // return: [was added, did page split]
    public Object[] insertRecordInPage(Page page, Record record, Schema schema, boolean lastPage,
            Integer positionIndex) {
        boolean shouldBeAdded = false;
        int indexToBeAdded = 0;
        Object[] rt = new Object[3];

        ArrayList<Record> pageRecords = page.getRecords();
        if (pageRecords.size() == 0) {
            pageRecords.add(record);
            rt[0] = true;
            rt[1] = false;
            rt[2] = 0;
            return rt;
        }

        int indexOfPrimaryKey = schema.getIndexOfPrimaryKey();

        if (positionIndex != null) {
            if (positionIndex < 0) {
                indexToBeAdded = 0;
                shouldBeAdded = true;
            } else {
                Record recordInPage = pageRecords.get(positionIndex);
                RecordAttribute primaryKeyRecord = recordInPage.getData().get(indexOfPrimaryKey);
                RecordAttribute primaryKeyData = record.getData().get(indexOfPrimaryKey);
                if (primaryKeyData.getType() == int.class) {
                    if ((Integer) primaryKeyRecord.getAttribute() < (Integer) primaryKeyData.getAttribute()) {
                        shouldBeAdded = true;
                        indexToBeAdded = positionIndex + 1;
                    }
                } else if (primaryKeyData.getType() == boolean.class) {
                    if (((Boolean) primaryKeyRecord.getAttribute()) && ((Boolean) primaryKeyData.getAttribute())) {
                        shouldBeAdded = true;
                        indexToBeAdded = positionIndex + 1;
                    }
                } else if (primaryKeyData.getType() == Character.class || primaryKeyData.getType() == String.class) {
                    String s1 = (String) primaryKeyRecord.getAttribute();
                    String s2 = (String) primaryKeyData.getAttribute();
                    if (s1.compareTo(s2) < 0) {
                        shouldBeAdded = true;
                        indexToBeAdded = positionIndex + 1;
                    }
                } else if (primaryKeyData.getType() == Double.class) {
                    if ((Double) primaryKeyRecord.getAttribute() < (Double) primaryKeyData.getAttribute()) {
                        shouldBeAdded = true;
                        indexToBeAdded = positionIndex + 1;
                    }
                }
            }
        } else {
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
        }

        if (lastPage && !shouldBeAdded) {
            shouldBeAdded = true;
            indexToBeAdded = pageRecords.size();
        }

        // couldn't find a spot for the primary key so return false
        if (!shouldBeAdded) {
            rt[0] = false;
            rt[1] = false;
            return rt;
        }

        // check remaining space and see if we need to split
        if (page.canRecordFitInPage(record)) {
            page.addRecord(indexToBeAdded, record);
            rt[0] = true;
            rt[1] = false;
            rt[2] = indexToBeAdded;
            return rt;
        } else {
            page.addRecord(indexToBeAdded, record);

            ArrayList<Record> records = page.getRecords();
            int splitIndex = records.size() / 2;
            int indexOfAdded = indexToBeAdded % splitIndex;

            splitPage(page);
            rt[0] = true;
            rt[1] = true;
            rt[2] = indexOfAdded;
            return rt;
        }
    }

    // split page and add record to correct page
    public void splitPage(Page page) {

        // we need to update the id of every other page so we loop over all of them and
        // increment the pageID
        int pageIndex = this.pageBuffer.getTotalPages(page.getSchema()) - 1;

        while (true) {
            if (pageIndex <= page.getPageID())
                break;
            Page pageToUpdate = this.pageBuffer.getPage(pageIndex, page.getSchema(), false);
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

    public void select(String[] selectAttr, String[] fromTableNames, String whereConditions, String[] orderbyAttr) {
        if (orderbyAttr != null) {
            for (String string : orderbyAttr) {
                System.out.println(string);
            }
        }

        ArrayList<Table> allTables = new ArrayList<>();
        for (String tableName : fromTableNames) {
            ArrayList<Record> recordsInTable = getRecordsFromtable(tableName);
            allTables.add(new Table(recordsInTable, tableName, catalog.getSchemaByName(tableName)));
        }

        Table finaltable = CartesianProduct.cartesianProduct(allTables, whereConditions, selectAttr);

        finaltable.getSchema().printSchema();
        System.out.println();
        for (Record record : finaltable.getRecords()) {
            record.printRecord();
            System.out.println();
        }
    }

    public ArrayList<Record> getRecordsFromtable(String tableName) {
        this.catalog.getSchemaByName(tableName);
        Schema schema = this.catalog.getSchemaByName(tableName);

        int pageIndex = 0;
        int pagesInTable = this.pageBuffer.getTotalPages(schema);

        ArrayList<Record> recordsInTable = new ArrayList<>();
        while (true) {
            if (pagesInTable <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);

            for (Record record : page.getRecords()) {
                recordsInTable.add(record);
            }

            pageIndex++;
        }
        return recordsInTable;
    }

    // add record to db
    public void addRecord(Record record, Schema schema) {
        // create new file if doesn't exist
        File db = new File(this.catalog.getFileOfRecord(record));
        try {
            db.createNewFile();
        } catch (IOException e) {
        }

        if (!this.catalog.useIndexing()) {
            int pageIndex = 0;
            int pagesInTable = this.pageBuffer.getTotalPages(schema);
            while (true) {
                if (pagesInTable <= pageIndex)
                    pageBuffer.createNewPage(schema);

                Page page = this.pageBuffer.getPage(pageIndex, schema, true);

                if ((boolean)(insertRecordInPage(page, record, schema, pagesInTable - 1 == pageIndex, null)[0]))
                    break;

                pageIndex++;
            }
        }
        else {

            Object primAttr = record.getData().get(schema.getIndexOfPrimaryKey()).getAttribute();

            BPlusTree bpt = schema.getBpt();

            int pagesInTable = this.pageBuffer.getTotalPages(schema);

            bpt.insert(primAttr, bpt.new PageInfo(-1, -1));
            PageInfo pi = bpt.getPositionToInsert(primAttr);

            Integer pageIndex = pi.pageIndex;
            Integer positionIndex = pi.positionIndex;

            if (pagesInTable <= pageIndex) {
                pageBuffer.createNewPage(schema);
            }

            int positionInserted;

            Page page = this.pageBuffer.getPage(pageIndex, schema, true);
            Object[] insertAttempt = insertRecordInPage(page, record, schema, pagesInTable - 1 == pageIndex, positionIndex);

            boolean wasSplit = (boolean) insertAttempt[1];

            if ((boolean) insertAttempt[0]) {
                positionInserted = (int) insertAttempt[2];
            } else {
                pageIndex++;
                if (pagesInTable <= pageIndex) {
                    pageBuffer.createNewPage(schema);
                }

                    page = this.pageBuffer.getPage(pageIndex, schema, true);
                    page = this.pageBuffer.getPage(pageIndex, schema, true);

                Object[] insertAttempt2 = insertRecordInPage(page, record, schema, pagesInTable - 1 == pageIndex,
                        positionIndex);
                if (!(boolean) insertAttempt2[0]) {
                    System.out.println("ERROR");
                }
                wasSplit = (boolean) insertAttempt2[1];
                positionInserted = (int) insertAttempt2[2];

            }

            bpt.updatePageInfo(primAttr, bpt.new PageInfo(pageIndex, positionInserted));

            if (wasSplit) {
                bpt.updateAllPagesPastAndIncluding(pageIndex, page, schema);
            }
            bpt.shiftRecordsInPageDown(pageIndex, positionInserted, primAttr);
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
                return false;
            }

            if (!page.isUniqueValueUnique(record)) {
                System.out.println("---ERROR---");
                System.out.println("Value is not unique\n");
                return false;
            }

            pageIndex++;
        }

        return true;
    }

    public boolean dropTable(Schema schema) {
        pageBuffer.removeSchemaFromBuffer(schema);
        pageBuffer.writer.deleteFile(schema);
        pageBuffer.writer.removeTableFromCatalog(schema);
        return true;
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
        this.catalog.addSchema(newSchema);

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

    public boolean delete(String tableName, String logic) {
        if (!this.catalog.useIndexing()) {
            this.catalog.getSchemaByName(tableName);
            Schema schema = this.catalog.getSchemaByName(tableName);

            int pageIndex = 0;

            int recordsDeleted = 0;

            while (true) {
                int pagesInTable = this.pageBuffer.getTotalPages(schema);
                if (pagesInTable <= pageIndex)
                    break;

                Page page = this.pageBuffer.getPage(pageIndex, schema, true);

                ArrayList<Record> newRecords = new ArrayList<>();
                for (Record record : page.getRecords()) {
                    if (!BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                        newRecords.add(record);
                    } else {
                        recordsDeleted++;
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

            System.out.println(recordsDeleted + " record(s) deleted.");
            return true;
        }
        else {
            //TODO FIX
            this.catalog.getSchemaByName(tableName);
            Schema schema = this.catalog.getSchemaByName(tableName);
            BPlusTree bpt = schema.getBpt();

            int pageIndex = 0;

            int recordsDeleted = 0;

            while (true) {
                int pagesInTable = this.pageBuffer.getTotalPages(schema);
                if (pagesInTable <= pageIndex)
                    break;

                Page page = this.pageBuffer.getPage(pageIndex, schema, true);

                ArrayList<Record> newRecords = new ArrayList<>();
                for (Record record : page.getRecords()) {
                    if (!BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                        newRecords.add(record);
                    } else {
                        Object key = record.getData().get(schema.getIndexOfPrimaryKey()).getAttribute();
                        bpt.delete(key);
                        recordsDeleted++;
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

            System.out.println(recordsDeleted + " record(s) deleted.");
            return true;
        }
    }

    private boolean deleteSingleRecord(String tableName, String logic) {
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
                return true;
            } else if (newRecords.size() != page.getRecords().size()) {
                page.setRecords(newRecords);
                return false;
            }

            pageIndex++;
        }

        return false;
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

    public boolean update(String tableName, String col, String val, String logic) {

        if (!this.catalog.useIndexing()) {
            this.catalog.getSchemaByName(tableName);
            Schema schema = this.catalog.getSchemaByName(tableName);

            int pageIndex = 0;
            int recordsUpdated = 0;
            while (true) {
                int pagesInTable = this.pageBuffer.getTotalPages(schema);
                if (pagesInTable <= pageIndex)
                    break;

                Page page = this.pageBuffer.getPage(pageIndex, schema, true);

                boolean wasPageDeleted = false;
                for (Record record : page.getRecords()) {
                    if (BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                        Record updatedRecord = new Record(record);

                        // needed for logic
                        int indexOfPrimaryKey = schema.getIndexOfPrimaryKey();
                        String nameOfPrimaryAttribute = schema.getAttributes().get(indexOfPrimaryKey).getAttributeName();
                        RecordAttribute recordAttribute = updatedRecord.getData().get(schema.getIndexOfPrimaryKey());
                        String valueOfPrimaryString = recordAttribute.getAttribute().toString();

                        // new logic for deleting
                        String logicString = nameOfPrimaryAttribute + " = " + valueOfPrimaryString;

                        // update record
                        int indexOfAttribute = schema.getIndexOfAttributeName(col);
                        String AttributeTypeAsString = schema.getAttributes().get(indexOfAttribute).getTypeAsString();
                        RecordAttribute recordAttributeToChange = updatedRecord.getData().get(indexOfAttribute);
                        Object o = recordAttributeToChange.getAttribute();
                        if (o.equals(Util.convertToType(AttributeTypeAsString, val))) {
                            continue;
                        }
                        recordAttributeToChange.setNewAttributeValue(Util.convertToType(AttributeTypeAsString, val));

                        wasPageDeleted = deleteSingleRecord(tableName, logicString);

                        if (!(doesRecordFollowConstraints(updatedRecord, tableName)
                                && schema.doesRecordFollowSchema(updatedRecord))) {
                            // if it fails to add after
                            addRecord(record, schema);
                            System.out.println("Unable to update record: ");
                            record.printRecord();
                            System.out.println(recordsUpdated + " records updated.");
                            return false;
                        }

                        // record is now updated
                        addRecord(updatedRecord, schema);
                        recordsUpdated++;
                    }
                }

                if (!wasPageDeleted) {
                    pageIndex++;
                }

            }

            System.out.println(recordsUpdated + " records updated.");
            return true;
        }
        else {
            // TODO FIX
            // b+tree
            this.catalog.getSchemaByName(tableName);
            Schema schema = this.catalog.getSchemaByName(tableName);
            BPlusTree bpt = schema.getBpt();

            int pageIndex = 0;
            int recordsUpdated = 0;
            while (true) {
                int pagesInTable = this.pageBuffer.getTotalPages(schema);
                if (pagesInTable <= pageIndex)
                    break;

                Page page = this.pageBuffer.getPage(pageIndex, schema, true);

                boolean wasPageDeleted = false;
                for (Record record : page.getRecords()) {
                    if (BooleanExpressionEvaluator.evaluate(logic, record, schema)) {
                        Record updatedRecord = new Record(record);

                        // needed for logic
                        int indexOfPrimaryKey = schema.getIndexOfPrimaryKey();
                        String nameOfPrimaryAttribute = schema.getAttributes().get(indexOfPrimaryKey).getAttributeName();
                        RecordAttribute recordAttribute = updatedRecord.getData().get(schema.getIndexOfPrimaryKey());
                        String valueOfPrimaryString = recordAttribute.getAttribute().toString();

                        // new logic for deleting
                        String logicString = nameOfPrimaryAttribute + " = " + valueOfPrimaryString;

                        // update record
                        int indexOfAttribute = schema.getIndexOfAttributeName(col);
                        String AttributeTypeAsString = schema.getAttributes().get(indexOfAttribute).getTypeAsString();
                        RecordAttribute recordAttributeToChange = updatedRecord.getData().get(indexOfAttribute);
                        Object o = recordAttributeToChange.getAttribute();
                        if (o.equals(Util.convertToType(AttributeTypeAsString, val))) {
                            continue;
                        }
                        recordAttributeToChange.setNewAttributeValue(Util.convertToType(AttributeTypeAsString, val));

                        wasPageDeleted = deleteSingleRecord(tableName, logicString);

                        // This takes the old record out from the b-plus tree
                        Object key = record.getData().get(schema.getIndexOfPrimaryKey()).getAttribute();
                        bpt.delete(key);

                        if (!(doesRecordFollowConstraints(updatedRecord, tableName)
                                && schema.doesRecordFollowSchema(updatedRecord))) {
                            // if it fails to add after
                            addRecord(record, schema);
                            System.out.println("Unable to update record: ");
                            record.printRecord();
                            System.out.println(recordsUpdated + " records updated.");
                            return false;
                        }

                        // record is now updated
                        // also adds the updated record back to the b-plus tree
                        addRecord(updatedRecord, schema);
                        recordsUpdated++;
                    }
                }

                if (!wasPageDeleted) {
                    pageIndex++;
                }

            }

            System.out.println(recordsUpdated + " records updated.");
            return true;
        }
    }

    // empty buffer
    public void writeBuffer() throws IOException {
        pageBuffer.clearBuffer();
    }

    public void printBuffer() {
        pageBuffer.printBuffer();
    }

}