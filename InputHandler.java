import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import DataTypes.SchemaDataType;

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
            ArrayList<SchemaDataType> schema = reader.getSchema("catalog.txt");
            for (SchemaDataType character : schema) {
                // character.getType();
                if (character.getLetter() == 'v') {
                    System.out.println(character.getLetter());
                    System.out.println(character.getLength());
                } else {
                    System.out.println(character.getLetter());
                }
            }
        }
        if (input[0].equals("display")) {
            ArrayList<SchemaDataType> schema = reader.getSchema("catalog.txt");
            ArrayList<ArrayList<Object>>  allRecords = storageManager.getAllRecords("tst.txt", schema);
            for (ArrayList<Object> record : allRecords) {
                for (Object col : record) {
                    System.out.println(col);
                }
            }
        }
    }
}
