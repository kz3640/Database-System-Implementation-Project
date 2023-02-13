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

    public boolean handleInput(String input) throws IOException {

        //String[] newInput = Arrays.copyOfRange(input, 1, input.length);

        // create table Name colName p i colName d

        if (input.toLowerCase().split("\\s+")[0].equals("create") && input.toLowerCase().split("\\s+")[1].equals("table")) {
            if (!input.split(" ")[2].split("\\(")[0].matches("[a-zA-Z0-9]+")) {
                System.err.println("Invalid table name.");
                return true;
                //TODO CHECK EXISTENCE
            }
            int startIndex = input.indexOf("(");
            int endIndex = input.lastIndexOf(")");
            String insideParens;
            if (startIndex != -1 && endIndex != -1) {
                insideParens = input.substring(startIndex + 1, endIndex).trim();
            } else {
                System.err.println("No parentheses found.");
                return true;
            }
            String[] tokenizedByComma = insideParens.split(",");
            String[][] fullyTokenized = new String[tokenizedByComma.length][];
            for (int i = 0; i < tokenizedByComma.length; i++) {
                fullyTokenized[i] = tokenizedByComma[i].split(" ");
            }
            boolean hasKey = false;
            for (int i = 0; i < tokenizedByComma.length; i++) {
                for (int j = 0; j < fullyTokenized[i].length; j++) {
                    if(j == 0 && !fullyTokenized[i][j].matches("[a-zA-Z0-9]+")) {
                        System.err.println(fullyTokenized[i][j] + " is not a valid name.");
                        return true;
                    }
                    if(j == 1 && !fullyTokenized[i][j].toLowerCase().matches("integer|double|boolean|char\\([0-9]+\\)|varchar\\([0-9]+\\)")) {
                        System.err.println(fullyTokenized[i][j] + " is not a valid type.");
                        return true;
                    }
                    if(j == 2) {
                        if(hasKey) {
                            System.err.println("Multiple keys present");
                            return true;
                        }
                        hasKey = true;
                    }
                    //TODO MAKE BICD
                }
            }
            System.out.println("Creating table...");
            //TODO CREATE TABLE
            return true;
            //storageManager.createCatalog(newInput);
        }

        if (input.split("\\s+")[0].toLowerCase().equals("select")) {
            String name = input.split("\\s+")[3];
            //TODO CHECK NAME EXISTS
        }

        if (input.split("\\s+")[0].toLowerCase().equals("insert") && input.split("\\s+")[1].toLowerCase().equals("into")) {
            String name = input.split("\\s+")[2];
            int startIndex = input.indexOf("(");
            int endIndex = input.lastIndexOf(")");
            String insideParens;
            if (startIndex != -1 && endIndex != -1) {
                insideParens = input.substring(startIndex + 1, endIndex).trim();
            } else {
                System.err.println("No parentheses found.");
                return true;
            }
            String[] tokenList = insideParens.split("\\s+");
            //TODO INSERT TOKENS

        }

        if (input.split("\\s+")[0].toLowerCase().equals("display") && input.split("\\s+")[1].toLowerCase().equals("schema")) {
            //TODO DISPLAY SCHEMA
        }

        if (input.split("\\s+")[0].toLowerCase().equals("display") && input.split("\\s+")[1].toLowerCase().equals("info")) {
            String name = input.split("\\s+")[2];
            if(!name.matches("[a-zA-Z0-9]+")) {
                System.err.println("Invalid name: " + name);
                return true;
                //TODO CHECK NAME EXISTS
            }
            //TODO DISPLAY INFO
        }

        if (input.split("\\s+")[0].toLowerCase().equals("quit")) {
            return false;
        }

        if (input.split("\\s+")[0].toLowerCase().equals("help")) {
            System.out.println("Create table command:");
            System.out.println("create table <name>(\n" +
                    "   <attr_name1> <attr_type1> primarykey,\n" +
                    "   <attr_name2> <attr_type2>,\n" +
                    "   ....\n" +
                    "   <attr_nameN> <attr_typeN>\n" +
                    ");\n");

            System.out.println("Select command:");
            System.out.println("select * " +
                    "from <name>;\n");

            System.out.println("Insert into command:");
            System.out.println("insert into <name> values <tuples>;\n");

            System.out.println("Display schema command:");
            System.out.println("display schema;\n");

            System.out.println("Display info command:");
            System.out.println("display info <name>;\n");

            return true;

        }

        System.err.println("Unknown command. Type help; for help.");
        return true;


        /*
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
        */
    }
}
