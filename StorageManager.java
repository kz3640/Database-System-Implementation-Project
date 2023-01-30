import java.io.IOException;
import java.util.ArrayList;

public class StorageManager {
    private BinaryWriter writer;
    private BinaryReader reader;
    
    public StorageManager(BinaryWriter writer, BinaryReader reader) {
        this.writer = writer;
        this.reader = reader;
        return;
    }

    public void createCatalog(String[] input) throws IOException {
        ArrayList<Object> catalog = writer.addDataToArray(input);
        writer.writeToFile(catalog, "catalog.txt");
    }

    public boolean checkData(ArrayList<Object> data) {
        ArrayList<Character> schema = reader.getSchema("catalog.txt");
        if (data.size() != schema.size()) {
            return false;
        }

        for (int index = 0; index < data.size(); index++) {
            switch (schema.get(index)) {
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


    public void addRecord(ArrayList<Object> data) throws IOException {
        boolean validInput = checkData(data);
        if (!validInput) {
            return;
        }
        writer.writeToFile(data, "tst.txt");
    }
}
