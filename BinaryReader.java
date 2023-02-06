import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    public void readPage(RandomAccessFile raf, ArrayList<SchemaAttribute> schema,
            ArrayList<ArrayList<Object>> dataList, int bytesToRead) throws IOException {
        long positionBefore = raf.getFilePointer();

        while (raf.getFilePointer() - positionBefore != bytesToRead) {
            // System.out.println("------");
            // System.out.println(fis.getChannel().position());
            // System.out.println(positionBefore);
            // System.out.println(fis.getChannel().position() - positionBefore);
            // System.out.println(bytesToRead);
            ArrayList<Object> data = new ArrayList<>();
            int dataLength = raf.readInt();
            // needed later

            int numBits = schema.size();
            int numBytes = (int) Math.ceil(numBits / 8.0);
            byte[] nullBitMap = new byte[numBytes];
            raf.read(nullBitMap);

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
                        data.add(raf.readInt());
                        break;
                    case 'b':
                        data.add(raf.readBoolean());
                        break;
                    case 'c':
                        data.add(raf.readChar());
                        break;
                    case 'v':
                        data.add(raf.readUTF());
                        break;
                    case 'd':
                        data.add(raf.readDouble());
                        break;
                }
                bitIndex++;
            }
            dataList.add(data);
        }
    }

    // schema needs to be removed from parameters bec page doesnt have access to it
    public ArrayList<ArrayList<Object>> getPage(String fileName, int pageNumber, Schema schema) throws IOException {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();

        int skipBytes = (pageNumber * 100) + 4;

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        raf.seek(skipBytes);

        int pageId = raf.readInt();
        int junkDataSize = raf.readInt();
        int bytesToRead = 100 - junkDataSize - 8;
        readPage(raf, schema.getAttributes(), dataList, bytesToRead);

        return dataList;
    }

    public ArrayList<ArrayList<Object>> getAllRecords(String fileName, ArrayList<SchemaAttribute> schema) throws IOException {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");

        if (raf.length() == 0) {
            raf.close();
            return dataList;
        }

        int pagesToRead = raf.readInt();
        int pagesRead = 0;
        while (pagesToRead > pagesRead) {
            int pageId = raf.readInt();
            int junkDataSize = raf.readInt();

            int bytesToRead = 100 - junkDataSize - 8;
            readPage(raf, schema, dataList, bytesToRead);
            raf.skipBytes(junkDataSize);
            pagesRead++;
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
