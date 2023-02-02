import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import Schema.Schema;
import Schema.SchemaAttribute;

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
        if (input[0].equals("create")) {
            storageManager.createCatalog(newInput);
        }
        if (input[0].equals("insert")) {
            ArrayList<Object> data = writer.addDataToArray(newInput);
            Schema schema = reader.getSchema("catalog.txt");
            storageManager.addRecord(data, schema);
        }
        if (input[0].equals("read")) {
            Schema schema = reader.getSchema("catalog.txt");
            schema.printTable();
        }
        if (input[0].equals("delete")) {
            File myObj = new File("database.txt");
            myObj.delete();
        }
        if (input[0].equals("display")) {
            ArrayList<SchemaAttribute> schema = reader.getSchema("catalog.txt").getAttributes();
            ArrayList<ArrayList<Object>>  allRecords = storageManager.getAllRecords("database.txt", schema);
            for (ArrayList<Object> record : allRecords) {
                System.out.print("\n record: " + allRecords.indexOf(record));
                for (Object col : record) {
                    System.out.print(" | ");
                    System.out.print(col);
                }
                System.out.print("\n");
            }
            System.out.print("\n");
        }
    }
}
