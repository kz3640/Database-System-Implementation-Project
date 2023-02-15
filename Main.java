import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import Buffer.PageBuffer;
import Catalog.Catalog;
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

        // read program parameters
        String path = null;
        int pageSize = 0;
        int bufferSize = 0;
        try {
            path = args[0];
            pageSize = Integer.parseInt(args[1]);
            bufferSize = Integer.parseInt(args[2]);
        } catch (Exception _e) {
            System.out.println("Unexpected program parameters. See usage");
            System.exit(0);
        }

        BinaryReader reader = new BinaryReader();
        BinaryWriter writer = new BinaryWriter();
        Catalog catalog;

        File file = new File(path);
        if (!(file.exists() && file.isDirectory())) {
            file.mkdirs();
            if (!new File(path + "catalog.txt").exists()) {
                System.out.println("No existing db found.");
                System.out.println("Creating new db at " + path);
                writer.createCatalog(path, pageSize);
            }
        }
        catalog = reader.getCatalog(path, pageSize, bufferSize);
        writer.setCatalog(catalog);

        PageBuffer pageBuffer = new PageBuffer(bufferSize, reader, writer, catalog);
        StorageManager storageManager = new StorageManager(pageBuffer, catalog);
        InputHandler inputHandler = new InputHandler(writer, reader, storageManager);

        Scanner scan = new Scanner(System.in);
        boolean programRunning = true;
        while (programRunning) {
            StringBuilder input = new StringBuilder();
            System.out.println("Enter command: ");
            while (true) {
                String line = scan.nextLine();

                // check to see if there is a semicolon in the input
                int semicolonIndex = line.indexOf(";");
                if (semicolonIndex != -1) {
                    // if there is then add everything before the semicolon to the stringbuilder
                    input.append(line.substring(0, semicolonIndex + 1));
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
