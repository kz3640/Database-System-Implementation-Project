import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class InputHandler {
    private BinaryWriter writer;
    private BinaryReader reader;
    private StorageManager storageManager;
    
    public InputHandler( BinaryWriter writer, BinaryReader reader, StorageManager storageManager) {
        this.writer = writer;
        this.reader = reader;
        this.storageManager = storageManager;
        return;
    }

    public void handleInput(String[] input) throws IOException {

        String[] newInput = Arrays.copyOfRange(input, 1, input.length);
            
        if (input[0].equals("catalog")) {
            storageManager.createCatalog(newInput);
        }
        if (input[0].equals("insert")) {
            ArrayList<Object> data = writer.addDataToArray(newInput);
            storageManager.addRecord(data);
        }
        if (input[0].equals("read")) {
            ArrayList<Character> schema = reader.getSchema("catalog.txt");
            for (Character character : schema) {
                System.out.println(character);
            }
        }
        if (input[0].equals("display")) {
            ArrayList<Character> schema = reader.getSchema("catalog.txt");
            ArrayList<Object> record = reader.readRecord("tst.txt", schema);
            for (Object object : record) {
                System.out.println(object);
            }
        }
    }
}
