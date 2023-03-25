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

        // remove debug items

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
        } else {
            System.out.println("DB already exist, using predefined page size.");
        }
        catalog = reader.getCatalog(path, pageSize, bufferSize);
        System.out.println("Page size: " + catalog.getPageSize());
        writer.setCatalog(catalog);

        PageBuffer pageBuffer = new PageBuffer(bufferSize, reader, writer, catalog);
        StorageManager storageManager = new StorageManager(pageBuffer, catalog);
        InputHandler inputHandler = new InputHandler(writer, storageManager);

        Scanner scan = new Scanner(System.in);
        boolean programRunning = true;
        while (programRunning) {
            StringBuilder input = new StringBuilder();
            System.out.println("Enter command: ");
            while (true) {
                String line = scan.nextLine();
                input.append(line + " ");

                // check to see if there is a semicolon in the input
                int semicolonIndex = findSemicolonOutsideQuotes(input.toString());
                if (semicolonIndex != -1) {
                    // if there is a semicolon, split the input at that point and handle each command separately
                    String[] commands = input.substring(0, semicolonIndex).trim().split(";");
                    for (String command : commands) {
                        String commandWithSemi = command + ";";
                        String finalInput = removeExtraWhitespace(commandWithSemi);
                        programRunning = inputHandler.handleInput(finalInput);
                        if (!programRunning) {
                            break;
                        }
                    }
                    break;
                }
            }
            // String finalInput = removeExtraWhitespace(input.toString());
            // programRunning = inputHandler.handleInput(finalInput);
        }
        scan.close();
    }

    // helper method to find the index of the first semicolon that appears outside
    // of a quoted string
    private static int findSemicolonOutsideQuotes(String input) {
        boolean inQuotes = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && c == ';') {
                return i;
            }
        }
        return -1;
    }

    private static String removeExtraWhitespace(String input) {
        StringBuilder output = new StringBuilder();
        input = input.trim();
        boolean insideQuotes = false;
        boolean prevCharWasSpace = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\"') {
                insideQuotes = !insideQuotes;
                output.append(c);
            } else if (!insideQuotes) {
                if (prevCharWasSpace && c == ' ') {
                    continue;
                }
                prevCharWasSpace = c == ' ';
                output.append(Character.toLowerCase(c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }
}
