import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        // if (args.length != 3) {
        // System.out.println("Expected 3 arguments, got " + args.length);
        // return;
        // }
        // String path = args[0];
        // int page_size = Integer.parseInt(args[1]);
        // int buffer_size = Integer.parseInt(args[2]);
        boolean quit = false;
        Scanner scan = new Scanner(System.in);

        BinaryWriter writer = new BinaryWriter();
        BinaryReader reader = new BinaryReader();
        StorageManager storageManager = new StorageManager(writer, reader);
        InputHandler inputHandler = new InputHandler(writer, reader, storageManager);

        // enter a command
        // - "catalog" to display the available catalog of items
        // - "read" followed by a file name to read data from a file
        // - "display" to display the current data
        // - "insert" to insert data

        // The user can enter data in the format 'insert <value1> <value2> ... <valueN>', where each value can be one of:
        // - int: an integer number
        // - boolean: true or false
        // - char: a single character, surrounded by single quotes (e.g. 'v')
        // - string: a sequence of characters, surrounded by double quotes (e.g."mystring")
        // - double: a decimal number
        // For example, the user could enter 'insert 10 "mystring" true 48.9 "the" 'v''
        // to insert the following values into the database:
        // - 10 as an int
        // - "mystring" as a String
        // - true as a boolean
        // - 48.9 as a double
        // - "the" as a String
        // - 'v' as a character

        while (!quit) {
            System.out.println("Enter command");
            String[] input = scan.nextLine().split(" ");
            inputHandler.handleInput(input);
        }
    }

    // 4.2 create catalog
    // user input
    // calls write data

    // 4.3 storage manager
    // What actually gets the data
    // calls read data
    // NOT THE SQL Parser

    // 4.3.1 Insert
    // read rules on how to insert

    // 4.4 Page buffer
    // Where information is stored
    // storage manager will interact with this

    // 5 Query processor
    // works with storage manager

    // 5.1 Create table
    // creates schema/catalog

    // 5.2 Select
    // display table

    // 5.3 Insert
    // Insert into table

    // 5.4 Disply schema
    // Display catalog

    // 5.5 Display info
    // display table information
}