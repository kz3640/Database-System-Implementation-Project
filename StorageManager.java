import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

    public boolean checkData(ArrayList<Object> data) {
        ArrayList<SchemaAttribute> schemaAttributes = this.schema.getAttributes();

        if (data.size() != schemaAttributes.size()) {
            return false;
        }

        for (int index = 0; index < data.size(); index++) {
            if (schemaAttributes.get(index).isNotNull() && data.get(index) == null) {
                return false;
            }
            if (!schemaAttributes.get(index).isNotNull() && data.get(index) == null) {
                continue;
            }
            switch (schemaAttributes.get(index).getLetter()) {
                case 'i':
                    if (!(data.get(index) instanceof Integer)) {
                        return false;
                    }
                    break;
                case 'v':
                    if (!(data.get(index) instanceof String)) {
                        return false;
                    }
                    break;
                case 'c':
                    if (!(data.get(index) instanceof Character)) {
                        return false;
                    }
                    break;
                case 'd':
                    if (!(data.get(index) instanceof Double)) {
                        return false;
                    }
                    break;
                case 'b':
                    if (!(data.get(index) instanceof Boolean)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean insertRecordInPage(Page page, ArrayList<Object> data) throws IOException {
        boolean shouldBeAdded = false;
        int indexToBeAdded = 0;

        ArrayList<ArrayList<Object>> pageRecords = page.getRecords();
        if (pageRecords.size() == 0) {
            pageRecords.add(data);
            return true;
        }

        int indexOfPrimaryKey = this.schema.getIndexOfPrimaryKey();

        for (int i = 0; i < pageRecords.size(); i++) {
            ArrayList<Object> record = pageRecords.get(i);
            Object primaryKeyRecord = record.get(indexOfPrimaryKey);
            Object primaryKeyData = data.get(indexOfPrimaryKey);
            if (primaryKeyData instanceof Integer) {
                if ((Integer) primaryKeyRecord > (Integer) primaryKeyData) {
                    System.out.println("index to add" + i);
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData instanceof Boolean) {
                if (!((Boolean) primaryKeyRecord) && ((Boolean) primaryKeyData)) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData instanceof Character) {
                if ((Character) primaryKeyRecord > (Character) primaryKeyData) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData instanceof String) {
                if (((String) primaryKeyRecord).compareTo((String) primaryKeyData) > 0) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            } else if (primaryKeyData instanceof Double) {
                if ((Double) primaryKeyRecord > (Double) primaryKeyData) {
                    shouldBeAdded = true;
                    indexToBeAdded = i;
                    break;
                }
            }
        }

        if (!shouldBeAdded) {
            return false;
        }

        if (page.canRecordFitInPage(data)){
            page.addRecord(indexToBeAdded, data);
        } else {
            page.addRecord(indexToBeAdded, data);
            splitPage(page);
        }

        return true;
    }

    public void splitPage(Page page) throws IOException {
        Page newPage = pageBuffer.insertNewPage(page.getPageID() + 1);

        ArrayList<ArrayList<Object>> records = page.getRecords();
        int splitIndex = records.size() / 2;
        ArrayList<ArrayList<Object>> firstHalf = new ArrayList<>(records.subList(0, splitIndex));
        ArrayList<ArrayList<Object>> secondHalf = new ArrayList<>(records.subList(splitIndex, records.size()));

        page.setRecords(firstHalf);
        newPage.setRecords(secondHalf);
        pageBuffer.addPageToBuffer(newPage);   
    }

    public void addRecord(String[] input) throws IOException {
        addRecord(addDataToArray(input));
    }

    public void addRecord(ArrayList<Object> data) throws IOException {

        if (this.schema.getAttributes() == null) {
            return;
        }

        // check to see if input is valid
        boolean validInput = checkData(data);
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

            if (insertRecordInPage(page, data))
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
