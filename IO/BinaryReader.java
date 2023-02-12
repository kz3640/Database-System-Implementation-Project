package IO;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Buffer.Page;
import Schema.BICD;
import Schema.Char;
import Schema.Schema;
import Schema.SchemaAttribute;
import Schema.Varchar;
import Record.Record;
import Record.RecordAttribute;

public class BinaryReader {
    private Schema schema;

    public BinaryReader(Schema schema) {
        this.schema = schema;
    }

    // get the schema from the catalog
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
                } else if (resultChar == 'c') {
                    int stringLength = dis.readInt();
                    attributes.add(new Char(attrName, stringLength, isPrimary, false));
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

    // retrieve the total amount of pages stored in the db
    public int getTotalPages() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.schema.getPath() + "database.txt", "rw");
        int totalPages = raf.readInt();
        raf.close();
        return totalPages;
    }

    // reads in a given page from the file
    public Page readPage(RandomAccessFile raf, int bytesToRead, int pageId) throws IOException {
        long positionBefore = raf.getFilePointer();

        ArrayList<SchemaAttribute> schemaAttributes = this.schema.getAttributes();
        ArrayList<Record> dataList = new ArrayList<Record>();

        while (raf.getFilePointer() - positionBefore != bytesToRead) {
            Record newRecord = new Record(new ArrayList<>());
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
                    newRecord.addAttributeToData(null);
                    bitIndex++;
                    continue;
                }
                switch (c.getLetter()) {
                    case 'i':
                        newRecord.addAttributeToData(new RecordAttribute(int.class, raf.readInt(), 0));
                        break;
                    case 'b':
                        newRecord.addAttributeToData(new RecordAttribute(boolean.class, raf.readBoolean(), 0));
                        break;
                    case 'c':
                        String charString = raf.readUTF();
                        int charsToStrip = raf.readInt();
                        charString = charString.substring(0, charString.length() - charsToStrip);
                        newRecord.addAttributeToData(
                                new RecordAttribute(Character.class, charString, charString.length()));
                        break;
                    case 'v':
                        newRecord.addAttributeToData(new RecordAttribute(String.class, raf.readUTF(), 0));
                        break;
                    case 'd':
                        newRecord.addAttributeToData(new RecordAttribute(double.class, raf.readDouble(), 0));
                        break;
                }
                bitIndex++;
            }
            dataList.add(newRecord);
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

    // read in a record from the file
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

    // debuggin
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
