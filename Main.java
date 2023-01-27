import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Expected 3 arguments, got " + args.length);
            return;
        }
        String path = args[0];
        int page_size = Integer.parseInt(args[1]);
        int buffer_size = Integer.parseInt(args[2]);
        Scanner input = new Scanner(System.in);
        boolean quit = false;
        String user_input = null;

        while(!quit){
            user_input = input.nextLine();
            quit = user_input.equals("quit");
        }
    }
}
