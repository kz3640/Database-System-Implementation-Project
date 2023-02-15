package IO;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Buffer.Page;
import Catalog.BICD;
import Catalog.Char;
import Catalog.Schema;
import Catalog.Catalog;
import Catalog.SchemaAttribute;
import Catalog.Varchar;
import Record.Record;
import Record.RecordAttribute;

public class BinaryReader {
    private Catalog catalog;

    public BinaryReader() {
        return;
    }

    // get the catalog from the catalog
    public Catalog getCatalog(String path, int pageSize, int bufferSize) {
        try (RandomAccessFile raf = new RandomAccessFile(path + "catalog.txt", "rw")) {
            int savedPageSize = raf.readInt();
            int totalTables = raf.readInt();

            if (savedPageSize != pageSize) {
                pageSize = savedPageSize;
            }

            Catalog catalog = new Catalog(path, pageSize, bufferSize);
            // catalog.setPageSize(pageSize);

            int tableIdToRead = 0;

            // loop through each table
            while (tableIdToRead < totalTables) {

                ArrayList<SchemaAttribute> attributes = new ArrayList<>();
                int sizeOfSchema = raf.readInt();
                String tableName = raf.readUTF();
                int bytesRead = tableName.length() + 2;
                while (bytesRead < sizeOfSchema) {
                    String attrName = raf.readUTF();
                    bytesRead = bytesRead + attrName.length() + 2;

                    String attrType = raf.readUTF();
                    bytesRead = bytesRead + attrType.length() + 2;

                    boolean isPrimary = false;
                    if (attrType.equals("primarykey")) {
                        isPrimary = true;
                        attrType = raf.readUTF();
                        bytesRead = bytesRead + attrType.length() + 2;
                    }
                    // is string, need length
                    if (attrType.equals("varchar")) {
                        int stringLength = raf.readInt();
                        attributes.add(new Varchar(attrName, stringLength, isPrimary, false));
                        bytesRead = bytesRead + 4;
                    } else if (attrType.equals("char")) {
                        int stringLength = raf.readInt();
                        attributes.add(new Char(attrName, stringLength, isPrimary, false));
                        bytesRead = bytesRead + 4;
                    } else {
                        attributes.add(new BICD(attrName, attrType, isPrimary, false));
                    }
                }
                Schema schema = new Schema(tableName, attributes, catalog);
                schema.setIndex(String.valueOf(tableIdToRead));
                catalog.addSchema(schema);

                tableIdToRead++;
            }

            this.catalog = catalog;
            raf.close();

            return catalog;
        } catch (

        IOException e) {
            e.printStackTrace();
        }

        return null;

        // ArrayList<SchemaAttribute> attributes = new ArrayList<>();
        // try (FileInputStream inputStream = new FileInputStream(this.catalog.getPath()
        // + "catalog.txt")) {
        // DataInputStream dis = new DataInputStream(inputStream);

        // int pageSize = dis.readInt();
        // String tableName = dis.readUTF();
        // while (dis.available() > 0) {
        // String attrName;
        // try {
        // attrName = dis.readUTF();
        // } catch (IOException e) {
        // break;
        // }
        // char resultChar = dis.readChar();
        // // isPrimary key
        // boolean isPrimary = false;
        // if (resultChar == 'p') {
        // isPrimary = true;
        // resultChar = dis.readChar();
        // }

        // // is string, need length
        // if (resultChar == 'v') {
        // int stringLength = dis.readInt();
        // attributes.add(new Varchar(attrName, stringLength, isPrimary, false));
        // } else if (resultChar == 'c') {
        // int stringLength = dis.readInt();
        // attributes.add(new Char(attrName, stringLength, isPrimary, false));
        // } else {
        // attributes.add(new BICD(attrName, resultChar, isPrimary, false));
        // }
        // }
        // this.catalog.setAttributes(attributes);
        // this.catalog.setTableName(tableName);
        // return this.catalog;
        // } catch (IOException e) {
        // }

        // return null;
    }

    // retrieve the total amount of pages stored in the db
    public int getTotalPages(String fileName) {
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(fileName, "rw");
            int totalPages = raf.readInt();
            raf.close();
            return totalPages;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // reads in a given page from the file
    public Page readPage(RandomAccessFile raf, int bytesToRead, int pageId, Schema schema) throws IOException {
        long positionBefore = raf.getFilePointer();

        ArrayList<SchemaAttribute> schemaAttributes = schema.getAttributes();
        ArrayList<Record> dataList = new ArrayList<Record>();

        while (raf.getFilePointer() - positionBefore != bytesToRead) {
            Record newRecord = new Record(new ArrayList<>(), this.catalog.fileNameToTableName(schema.getFileName()));
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
                switch (c.getTypeAsString()) {
                    case "integer":
                        newRecord.addAttributeToData(new RecordAttribute(int.class, raf.readInt(), 0));
                        break;
                    case "boolean":
                        newRecord.addAttributeToData(new RecordAttribute(boolean.class, raf.readBoolean(), 0));
                        break;
                    case "char":
                        String charString = raf.readUTF();
                        int charsToStrip = raf.readInt();
                        charString = charString.substring(0, charString.length() - charsToStrip);
                        newRecord.addAttributeToData(
                                new RecordAttribute(Character.class, charString, charString.length()));
                        break;
                    case "varchar":
                        newRecord.addAttributeToData(new RecordAttribute(String.class, raf.readUTF(), 0));
                        break;
                    case "double":
                        newRecord.addAttributeToData(new RecordAttribute(double.class, raf.readDouble(), 0));
                        break;
                }
                bitIndex++;
            }
            dataList.add(newRecord);
        }

        return new Page(pageId, dataList, this.catalog, schema.getFileName());
    }

    // catalog needs to be removed from parameters bec page doesnt have access to it
    public Page getPage(int pageNumber, Schema schema) {
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(schema.getFileName(), "rw");
            int totalPages = raf.readInt();

            // 0 pages means no pages
            // page number 0 means the first page
            if (totalPages <= pageNumber) {
                raf.close();
                System.out.println("No page found at index " + pageNumber);
                return null;
            }
            int pageIndex = pageNumber;

            int skipBytes = (pageIndex * this.catalog.getPageSize()) + 4;
            raf.seek(skipBytes);

            int pageId = raf.readInt();
            int junkDataSize = raf.readInt();
            int bytesToRead = this.catalog.getPageSize() - junkDataSize - 8;
            Page page = readPage(raf, bytesToRead, pageId, schema);

            return page;
        } catch (IOException e) {
            return null;
        }
    }

    public int getRecordAmmount(Schema schema) {
        int records = 0;
        int pageIndex = 0;
        int pagesInTable = this.getTotalPages(schema.getFileName());
        while (true) {
            if (pagesInTable == pageIndex)
                break;
            Page page = this.getPage(pageIndex, schema);
            records += page.getRecords().size();
            pageIndex++;
        }
        return records;
    }

    public void printTableInfo(String tableName) {

    }

    // debuggin
    // public void printDB() throws IOException {
    // RandomAccessFile raf = new RandomAccessFile(this.catalog.getPath() +
    // "database.txt", "rw");

    // if (raf.length() == 0) {
    // System.out.println("db not init");
    // raf.close();
    // }

    // int pagesToRead = raf.readInt();
    // int pagesRead = 0;
    // while (pagesToRead > pagesRead) {
    // getPage(pagesRead, fileName).printPage();
    // pagesRead++;
    // }
    // }

}
