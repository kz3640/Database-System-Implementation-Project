import java.io.DataInputStream;import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import DataTypes.OtherDataType;
import DataTypes.SchemaDataType;
import DataTypes.Varchar;

public class BinaryReader {
    

    public BinaryReader() {
        return;
    }

    public ArrayList<SchemaDataType> getSchema(String filePath) {
        ArrayList<SchemaDataType> characters = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            DataInputStream dis = new DataInputStream(inputStream);
            while (dis.available() > 0) {
                char resultChar = dis.readChar();
                if (resultChar == 'v') {
                    int stringLength = dis.readInt();
                    characters.add(new Varchar(stringLength));
                } else {
                    characters.add(new OtherDataType(resultChar));
                }
            }
        } catch (IOException e) {}
        return characters;
    }

    public ArrayList<Object> readRecord(String fileName, ArrayList<SchemaDataType> schema) throws IOException {
        ArrayList<Object> data = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for (SchemaDataType c : schema) {
                switch (c.getLetter()) {
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
