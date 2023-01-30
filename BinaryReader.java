import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BinaryReader {
    

    public BinaryReader() {
        return;
    }

    public ArrayList<Character> getSchema(String filePath) {
        ArrayList<Character> characters = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            DataInputStream dis = new DataInputStream(inputStream);
            while (dis.available() > 0) {
                char resultChar = dis.readChar();
                characters.add(resultChar);
            }
        } catch (IOException e) {
        }
        return characters;
    }

    public ArrayList<Object> readRecord(String fileName, ArrayList<Character> schema) throws IOException {
        ArrayList<Object> data = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for (Character c : schema) {
                switch (c) {
                    case 'i':
                        data.add(dis.readInt());
                        break;
                    case 'b':
                        data.add(dis.readBoolean());
                        break;
                    case 'c':
                        data.add(dis.readChar());
                        break;
                    case 'v':
                        data.add(dis.readUTF());
                        break;
                    case 'd':
                        data.add(dis.readDouble());
                        break;
                }
            }
        }
        return data;
    }



}
