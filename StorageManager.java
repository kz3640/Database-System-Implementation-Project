import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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

    public ArrayList<ArrayList<Object>> getAllRecords(String fileName, ArrayList<SchemaAttribute> schema) {
        return reader.getAllRecords(fileName, schema);
    }

    public void addRecord(ArrayList<Object> data) throws IOException {
        boolean validInput = checkData(data);
        if (!validInput) {
            System.out.println("Invalid input");
            return;
        }
        int recordSize = writer.calculateBytes(data);
        writer.writeRecordToFile(data, "database.txt", recordSize);
    }
}
