import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import Schema.Schema;
import Schema.SchemaAttribute;

public class StorageManager {
    private BinaryWriter writer;
    private BinaryReader reader;

    public StorageManager(BinaryWriter writer, BinaryReader reader) {
        this.writer = writer;
        this.reader = reader;
        return;
    }

    public void createCatalog(String[] input) throws IOException {
        // parse insert table command
        ArrayList<Object> catalog = writer.addDataToArray(input);
        writer.writeSchemaToFile(catalog, "catalog.txt");
    }

    public boolean checkData(ArrayList<Object> data) {
        ArrayList<SchemaAttribute> schema = reader.getSchema("catalog.txt").getAttributes();
        if (data.size() != schema.size()) {
            return false;
        }

        for (int index = 0; index < data.size(); index++) {
            if (schema.get(index).isNotNull() && data.get(index) == null) {
                return false;
            }
            if (!schema.get(index).isNotNull() && data.get(index) == null) {
                continue;
            }
            switch (schema.get(index).getLetter()) {
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

    public ArrayList<ArrayList<Object>> getAllRecords(String fileName, ArrayList<SchemaAttribute> schema) throws IOException {
        return reader.getAllRecords(fileName, schema);
    }

    public void insertRecordInPage(ArrayList<ArrayList<Object>> allRecords, ArrayList<Object> data,
            int indexOfPrimaryKey) {
        if (allRecords.size() == 0) {
            allRecords.add(data);
            return;
        }

        for (int i = 0; i < allRecords.size(); i++) {
            ArrayList<Object> record = allRecords.get(i);
            Object primaryKeyRecord = record.get(indexOfPrimaryKey);
            Object primaryKeyData = data.get(indexOfPrimaryKey);
            if (primaryKeyData instanceof Integer) {
                if ((Integer) primaryKeyRecord > (Integer) primaryKeyData) {
                    allRecords.add(i, data);
                    return;
                }
            } else if (primaryKeyData instanceof Boolean) {
                if (!((Boolean) primaryKeyRecord) && ((Boolean) primaryKeyData)) {
                    allRecords.add(i, data);
                    return;
                }
            } else if (primaryKeyData instanceof Character) {
                if ((Character) primaryKeyRecord > (Character) primaryKeyData) {
                    allRecords.add(i, data);
                    return;
                }
            } else if (primaryKeyData instanceof String) {
                if (((String) primaryKeyRecord).compareTo((String) primaryKeyData) > 0) {
                    allRecords.add(i, data);
                    return;
                }
            } else if (primaryKeyData instanceof Double) {
                if ((Double) primaryKeyRecord > (Double) primaryKeyData) {
                    allRecords.add(i, data);
                    return;
                }
            }
        }
        allRecords.add(data);
    }

    public void addRecord(ArrayList<Object> data, Schema schema) throws IOException {
        boolean validInput = checkData(data);
        if (!validInput) {
            System.out.println("Invalid input");
            return;
        }

        File db = new File("database.txt");
        db.createNewFile();

        ArrayList<ArrayList<Object>> allRecords = getAllRecords("database.txt", schema.getAttributes());

        insertRecordInPage(allRecords, data, schema.getIndexOfPrimaryKey());

        // writes a specfic page to the db file
        // writer.writePage(allRecords, "database.txt", schema, 2, 100);
        writer.writeAll(allRecords, "database.txt", schema);
    }
}
