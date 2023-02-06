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


        // * create (create table)
        // ex. create tableName col1Name p i col2Name s col3Name d

        // * insert (insert into table)
        // ex. insert 1 mystring 10.0

        // * select (select * from table)
        // ex. select

        // * catalog (shows schema)
        // ex. catalog


        // * delete (deletes db file)
        // ex. delete

        while (!quit) {
            System.out.println("Enter command");
            String[] input = scan.nextLine().split(" ");
            inputHandler.handleInput(input);
        }
        scan.close();
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