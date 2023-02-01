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
        writer.writeToFile(catalog, "catalog.txt", null);
    }

    public boolean checkData(ArrayList<Object> data) {
        ArrayList<SchemaAttribute> schema = reader.getSchema("catalog.txt").getAttributes();
        if (data.size() != schema.size()) {
            return false;
        }

        for (int index = 0; index < data.size(); index++) {
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


        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            while (true) {
                ArrayList<Object> data = new ArrayList<>();
                int dataLength = dis.readInt();
                for (SchemaAttribute c : schema) {
                    switch (c.getLetter()) {
                        case 'i':
                            data.add(dis.readInt());
                            break;
                        case 'b':
                            data.add(dis.readBoolean());
                            break;
                        case 'c':
                            data.add(dis.readChar());
                            break;
                        case 'v':
                            data.add(dis.readUTF());
                            break;
                        case 'd':
                            data.add(dis.readDouble());
                            break;
                    }
                }
                dataList.add(data);
            }
        } catch (java.io.EOFException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
       
    }

    public void addRecord(ArrayList<Object> data) throws IOException {
        boolean validInput = checkData(data);
        if (!validInput) {
            return;
        }
        int recordSize = writer.calculateBytes(data);
        writer.writeToFile(data, "database.txt", recordSize);
    }
}
