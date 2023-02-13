import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import Buffer.PageBuffer;
import Schema.Schema;
import StorageManager.StorageManager;
import IO.BinaryWriter;
import InputHandler.InputHandler;
import IO.BinaryReader;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Expected 3 arguments, got " + args.length);
            return;
        }

        if (!args[0].substring(args[0].length() - 1).equals("/")) {
            System.out.println("First arg must be a full path");
            return;
        }

        String path = null;
        int pageSize = 0;
        int bufferSize = 0;
        try {
            path = args[0];
            pageSize = Integer.parseInt(args[1]);
            bufferSize = Integer.parseInt(args[2]);
        } catch (Exception _e) {
            System.out.println("Unexpected program parameters");
            System.exit(0);
        }
        File file = new File(path);

        Schema schema = new Schema(null, path, pageSize, null);

        BinaryReader reader = new BinaryReader(schema);
        BinaryWriter writer = new BinaryWriter(schema);
        PageBuffer pageBuffer = new PageBuffer(pageSize, bufferSize, reader, writer);
        StorageManager storageManager = new StorageManager(pageBuffer, schema);
        InputHandler inputHandler = new InputHandler(writer, reader, storageManager);

        if (file.exists() && file.isDirectory()) {
            if (new File(path + "catalog.txt").exists()) {
                schema = reader.getSchema();
            }
        } else {
            file.mkdirs();
        }

        if (!new File(path + "database.txt").exists()) {
            pageBuffer.initDB();
        }


        Scanner scan = new Scanner(System.in);
        boolean programRunning = true;
        while(programRunning) {
            StringBuilder input = new StringBuilder();
            System.out.println("Enter command: ");
            while (true) {
                String line = scan.nextLine();
                if (line.trim().endsWith(";")) {
                    input.append(line.substring(0, line.length() - 1));
                    break;
                }
                input.append(line + " ");
            }
            String finalInput = input.toString().replaceAll("\\s+", " ");
            programRunning = inputHandler.handleInput(finalInput);
        }
        scan.close();
    }
}
