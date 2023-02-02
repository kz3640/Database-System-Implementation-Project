import java.io.DataInputStream;
import java.io.FileInputStream;
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
                    attributes.add(new Varchar(attrName, stringLength, isPrimary, false));
                } else {
                    attributes.add(new BICD(attrName, resultChar, isPrimary, false));
                }
            }
            return new Schema(tableName, attributes);
        } catch (IOException e) {
        }

        return null;
    }

    public ArrayList<ArrayList<Object>> getAllRecords(String fileName, ArrayList<SchemaAttribute> schema) {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            while (true) {
                ArrayList<Object> data = new ArrayList<>();
                int dataLength = dis.readInt();
                // needed later

                int numBits = schema.size();
                int numBytes = (int) Math.ceil(numBits / 8.0);
                byte[] nullBitMap = new byte[numBytes];
                dis.read(nullBitMap);

                int bitIndex = 0;
                // read null bit map here.
                for (SchemaAttribute c : schema) {
                    if ((nullBitMap[bitIndex / 8] & (1 << (bitIndex % 8))) != 0) {
                        data.add(null);
                        bitIndex++;
                        continue;
                    }
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
                    bitIndex++;
                }
                dataList.add(data);
            }
        } catch (java.io.EOFException e) {
        } catch (IOException e) {
        }
        return dataList;
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
