import java.io.DataInputStream;import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import Schema.BICD;
import Schema.Schema;
import Schema.SchemaAttribute;
import Schema.Varchar;

public class BinaryReader {
    

    public BinaryReader() {
        return;
    }

    public Schema getSchema(String filePath) {
        ArrayList<SchemaAttribute> attributes = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            DataInputStream dis = new DataInputStream(inputStream);
            
            String tableName = dis.readUTF();
            while (dis.available() > 0) {
                String attrName;
                try { 
                    attrName = dis.readUTF();
                } catch (IOException e) {
                    break;
                }
                char resultChar = dis.readChar();

                // isPrimary key
                boolean isPrimary = false;
                if (resultChar == 'p') {
                    isPrimary = true;
                    resultChar = dis.readChar();
                }

                // is string, need length
                if (resultChar == 'v') {
                    int stringLength = dis.readInt();
                    attributes.add(new Varchar(attrName, stringLength, isPrimary));
                } else {
                    attributes.add(new BICD(attrName, resultChar, isPrimary));
                }
            }
            return new Schema(tableName, attributes);
        } catch (IOException e) {}

        return null;
    }

    public ArrayList<Object> readRecord(String fileName, ArrayList<SchemaAttribute> schema) throws IOException {
        ArrayList<Object> data = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for (SchemaAttribute c : schema) {
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
