package StorageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Buffer.PageBuffer;
import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Schema.BICD;
import Schema.Char;
import Schema.Schema;
import Schema.SchemaAttribute;

public class StorageManager {
    private Schema schema;
    public PageBuffer pageBuffer;

    public StorageManager(PageBuffer pageBuffer, Schema schema) {
        this.pageBuffer = pageBuffer;
        this.schema = schema;
        return;
    }

    public Schema getSchema() {
        return schema;
    }

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

    public void createCatalog(String[] input) throws IOException {
        // parse insert table command
        ArrayList<Object> catalog = addDataToArray(input);
        catalog.add(0, this.schema.getPageSize());
        pageBuffer.writeSchemaToFile(catalog);
        this.schema = pageBuffer.getSchema();
    }

    public boolean checkData(Record record) {
        ArrayList<SchemaAttribute> schemaAttributes = this.schema.getAttributes();

        ArrayList<RecordAttribute> recordAttributes = record.getData();
        if (recordAttributes.size() != schemaAttributes.size()) {
            return false;
        }

        for (int index = 0; index < recordAttributes.size(); index++) {
            if (schemaAttributes.get(index).isNotNull() && recordAttributes.get(index) == null) {
                return false;
            }
            if (!schemaAttributes.get(index).isNotNull() && recordAttributes.get(index) == null) {
                continue;
            }
            switch (schemaAttributes.get(index).getLetter()) {
                case 'i':
                    if (!(recordAttributes.get(index).getType() == int.class)) {
                        return false;
                    }
                    break;
                case 'v':
                    if (!(recordAttributes.get(index).getType() == String.class)) {
                        return false;
                    }
                    break;
                case 'c':
                    if (!(recordAttributes.get(index).getType() == Character.class)) {
                        return false;
                    }
                    break;
                case 'd':
                    if (!(recordAttributes.get(index).getType() == double.class)) {
                        return false;
                    }
                    break;
                case 'b':
                    if (!(recordAttributes.get(index).getType() == boolean.class)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean insertRecordInPage(Page page, Record record) throws IOException {
        boolean shouldBeAdded = false;
        int indexToBeAdded = 0;

        ArrayList<Record> pageRecords = page.getRecords();
        if (pageRecords.size() == 0) {
            pageRecords.add(record);
            return true;
        }

        int indexOfPrimaryKey = this.schema.getIndexOfPrimaryKey();

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
                if ((Character) primaryKeyRecord.getAttribute() > (Character) primaryKeyData.getAttribute()) {
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

        if (!shouldBeAdded) {
            return false;
        }

        if (page.canRecordFitInPage(record)) {
            page.addRecord(indexToBeAdded, record);
        } else {
            page.addRecord(indexToBeAdded, record);
            splitPage(page);
        }

        return true;
    }

    public void splitPage(Page page) throws IOException {
        Page newPage = pageBuffer.insertNewPage(page.getPageID() + 1);

        ArrayList<Record> records = page.getRecords();
        int splitIndex = records.size() / 2;
        ArrayList<Record> firstHalf = new ArrayList<>(records.subList(0, splitIndex));
        ArrayList<Record> secondHalf = new ArrayList<>(records.subList(splitIndex, records.size()));

        page.setRecords(firstHalf);
        newPage.setRecords(secondHalf);
        pageBuffer.addPageToBuffer(newPage);
    }

    public Record stringToRecord(String[] input) {
        ArrayList<RecordAttribute> recordData = new ArrayList<RecordAttribute>();
        int index = 0;
        for (String s : input) {
            if (s.equals("null")) {
                recordData.add(new RecordAttribute(null, null, 0));
                index++;
                continue;
            }
            try {
                int i = Integer.parseInt(s);
                recordData.add(new RecordAttribute(int.class, i, 0));
            } catch (NumberFormatException e) {
                String lowerString = s.toLowerCase();
                if (lowerString.equals("true") || lowerString.equals("false")) {
                    boolean b = Boolean.parseBoolean(s);
                    recordData.add(new RecordAttribute(boolean.class, b, 0));
                } else {
                    try {
                        double d = Double.parseDouble(s);
                        recordData.add(new RecordAttribute(double.class, d, 0));
                    } catch (NumberFormatException exx) {
                        if (schema.getAttributes().get(index).getLetter() == 'c') {
                            int charLength = this.schema.getAttributes().get(index).getLength();
                            recordData.add(new RecordAttribute(Character.class, s, charLength));
                        } else if (schema.getAttributes().get(index).getLetter() == 'v') {
                            recordData.add(new RecordAttribute(String.class, s, 0));
                        }
                    }
                }
            }
            index++;
        }


        Record r = new Record(recordData);

        return r;
    }

    public void addRecord(String[] input) throws IOException {
        addRecord(stringToRecord(input));
    }

    public void addRecord(Record record) throws IOException {

        if (this.schema.getAttributes() == null) {
            return;
        }

        // check to see if input is valid
        boolean validInput = checkData(record);
        if (!validInput) {
            System.out.println("Invalid input");
            return;
        }

        // create new file if doesn't exist
        File db = new File(this.schema.getPath() + "database.txt");
        db.createNewFile();

        int pageIndex = 0;
        while (true) {
            if (this.pageBuffer.getTotalPages() <= pageIndex)
                pageBuffer.createNewPage();

            Page page = this.pageBuffer.getPage(pageIndex);

            if (insertRecordInPage(page, record))
                break;

            pageIndex++;
        }
    }

    public void printAllRecords() throws IOException {
        int pageIndex = 0;
        while (true) {

            if (this.pageBuffer.getTotalPages() <= pageIndex)
                break;

            Page page = this.pageBuffer.getPage(pageIndex);

            page.printPage();

            pageIndex++;
        }
    }

    public void writeBuffer() throws IOException {
        pageBuffer.clearBuffer();
    }

    public void printDB() throws IOException {
        pageBuffer.printDB();
    }

    public void printBuffer() {
        pageBuffer.printBuffer();
    }
}