import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Schema.BICD;
import Schema.Schema;
import Schema.SchemaAttribute;
import Schema.Varchar;

public class BinaryReader {
    private Schema schema;

    public BinaryReader(Schema schema) {
        this.schema = schema;
    }

    public Schema getSchema() {
        ArrayList<SchemaAttribute> attributes = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(this.schema.getPath() + "catalog.txt")) {
            DataInputStream dis = new DataInputStream(inputStream);

            int pageSize = dis.readInt();
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
            this.schema.setAttributes(attributes);
            this.schema.setTableName(tableName);
            return this.schema;
        } catch (IOException e) {
        }

        return null;
    }

    public Page readPage(RandomAccessFile raf, int bytesToRead, int pageId) throws IOException {
        long positionBefore = raf.getFilePointer();

        ArrayList<SchemaAttribute> schemaAttributes = this.schema.getAttributes();
        ArrayList<ArrayList<Object>> dataList = new ArrayList<ArrayList<Object>>();

        while (raf.getFilePointer() - positionBefore != bytesToRead) {
            ArrayList<Object> data = new ArrayList<>();
            int dataLength = raf.readInt();
            // needed later

            int numBits = schemaAttributes.size();
            int numBytes = (int) Math.ceil(numBits / 8.0);
            byte[] nullBitMap = new byte[numBytes];
            raf.read(nullBitMap);

            int bitIndex = 0;
            // read null bit map here.
            for (SchemaAttribute c : schemaAttributes) {
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

        return new Page(pageId, dataList, this.schema);
    }

    // schema needs to be removed from parameters bec page doesnt have access to it
    public Page getPage(int pageNumber) throws IOException {
        String fileName = this.schema.getPath() + "database.txt";

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");

        int totalPages = raf.readInt();

        // 0 pages means no pages
        // page number 0 means the first page
        if (totalPages <= pageNumber) {
            raf.close();
            System.out.println("No page found at index " + pageNumber);
            return null;
        }
        int pageIndex = pageNumber;

        int skipBytes = (pageIndex * this.schema.getPageSize()) + 4;
        raf.seek(skipBytes);

        int pageId = raf.readInt();
        int junkDataSize = raf.readInt();
        int bytesToRead = this.schema.getPageSize() - junkDataSize - 8;
        Page page = readPage(raf, bytesToRead, pageId);

        return page;
    }

    public ArrayList<Object> readRecord(String fileName) throws IOException {
        ArrayList<Object> data = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for (SchemaAttribute c : this.schema.getAttributes()) {
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

    public void printDB() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.schema.getPath() + "database.txt", "rw");

        if (raf.length() == 0) {
            System.out.println("db not init");
            raf.close();
        }

        int pagesToRead = raf.readInt();
        int pagesRead = 0;
        while (pagesToRead > pagesRead) {
            getPage(pagesRead).printPage();
            pagesRead++;
        }
    }

}
