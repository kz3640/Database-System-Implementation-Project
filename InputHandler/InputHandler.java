package InputHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import IO.BinaryWriter;
import IO.BinaryReader;
import StorageManager.StorageManager;
import Schema.Schema;
import Schema.SchemaAttribute;

public class InputHandler {
    private BinaryWriter writer;
    private BinaryReader reader;
    private StorageManager storageManager;

    public InputHandler(BinaryWriter writer, BinaryReader reader, StorageManager storageManager) {
        this.writer = writer;
        this.reader = reader;
        this.storageManager = storageManager;
        return;
    }

    public void handleInput(String[] input) throws IOException {

        String[] newInput = Arrays.copyOfRange(input, 1, input.length);

        // create tableName colName p i colName d
        if (input[0].equals("create")) {
            storageManager.createCatalog(newInput);
        }

        // insert 10 11.1
        if (input[0].equals("insert")) {
            storageManager.addRecord(newInput);
        }

        // catalog
        if (input[0].equals("catalog")) {
            Schema schema = storageManager.getSchema();
            schema.printTable();
        }

        // select
        if (input[0].equals("select")) {
            storageManager.printAllRecords();
        }
        // write
        if (input[0].equals("write")) {
            storageManager.writeBuffer();
        }
        
        // only for testing
        if (input[0].equals("delete")) {
            File myObj = new File(storageManager.getSchema().getPath() + "database.txt");
            myObj.delete();
            storageManager.pageBuffer.initDB();
        }
        if (input[0].equals("db")) {
            storageManager.printDB();
        }
        if (input[0].equals("buf")) {
            storageManager.printBuffer();
        }
    }
}
