package IO;

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
import Tree.BPlusTree;

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
                    boolean isUnique = false;
                    if (attrType.equals("unique")) {
                        isUnique = true;
                        attrType = raf.readUTF();
                        bytesRead = bytesRead + attrType.length() + 2;
                    }
                    boolean notNull = false;
                    if (attrType.equals("notnull")) {
                        notNull = true;
                        attrType = raf.readUTF();
                        bytesRead = bytesRead + attrType.length() + 2;
                    }
                    // is string, need length
                    if (attrType.equals("varchar")) {
                        int stringLength = raf.readInt();
                        attributes.add(new Varchar(attrName, stringLength, isPrimary, notNull, isUnique, null));
                        bytesRead = bytesRead + 4;
                    } else if (attrType.equals("char")) {
                        int stringLength = raf.readInt();
                        attributes.add(new Char(attrName, stringLength, isPrimary, notNull, isUnique, null));
                        bytesRead = bytesRead + 4;
                    } else {
                        attributes.add(new BICD(attrName, attrType, isPrimary, notNull, isUnique, null));
                    }
                }
                
                Schema schema = new Schema(tableName, attributes, catalog, null);
                schema.setIndex(String.valueOf(tableIdToRead));

                BPlusTree bpt = readBPT(schema);
                schema.addBPT(bpt);

                catalog.addSchema(schema);

                tableIdToRead++;
            }

            this.catalog = catalog;
            raf.close();

            return catalog;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public BPlusTree readBPT(Schema schema) {

        String type = "int";
        for (SchemaAttribute schemaAttribute : schema.getAttributes()) {
            if (schemaAttribute.isPrimaryKey()) {
                if (schemaAttribute.getTypeAsString().equals("varchar")
                        || schemaAttribute.getTypeAsString().equals("char")) {
                    type = "string";
                } else {
                    type = schemaAttribute.getTypeAsString();
                }
            }
        }

        BPlusTree bpt = null;
        bpt = new BPlusTree(4, type);

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(schema.getBPlusTreeFileName(), "rw");
            int keys = raf.readInt();

            for (int i = 0; i < keys; i++) {
                Object key;
                if (bpt.getType().equals("integer")) {
                    key = raf.readInt();
                } else if (bpt.getType().equals("stirng")) {
                    key = raf.readUTF();
                } else if (bpt.getType().equals("double")) {
                    key = raf.readDouble();
                } else {
                    key = raf.readBoolean();
                }
                int pageIndex = raf.readInt();
                int pagePosition = raf.readInt();

                bpt.insert(key, bpt.new PageInfo(pageIndex, pagePosition));
            }

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return bpt;
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
            // read null bit map here.

            int bitIndex = 0;
            for (SchemaAttribute c : schemaAttributes) {
                if ((nullBitMap[bitIndex / 8] & (1 << (bitIndex % 8))) != 0) {
                    newRecord.addAttributeToData(new RecordAttribute(null, null, 0));
                } else {
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

            raf.close();
            return page;
        } catch (IOException e) {
            return null;
        }
    }
}
