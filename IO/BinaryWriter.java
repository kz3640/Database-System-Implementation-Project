package IO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Catalog.Catalog;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Util.Util;

public class BinaryWriter {
    private Catalog catalog;

    public BinaryWriter() {
        return;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public void createCatalog(String path, int pageSize) {
        String fileName = path + "catalog.txt";
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            raf.writeInt(pageSize);
            raf.writeInt(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSchemaToFile(Schema schema) {
        String fileName = catalog.getPath() + "catalog.txt";
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {

            // increment tables count
            raf.seek(4);
            int numTables = raf.readInt() + 1;
            schema.setIndex(String.valueOf(numTables - 1));
            raf.seek(4);
            raf.writeInt(numTables);

            // go to end of file to write the information
            int lengthOfSchema = 0;
            raf.seek(raf.length());
            long positionOfLength = raf.getFilePointer();
            raf.writeInt(0); // length of schema in bytes

            raf.writeUTF(schema.getTableName());
            lengthOfSchema = lengthOfSchema + schema.getTableName().length() + 2;

            // write each attribute to the file
            for (SchemaAttribute attribute : schema.getAttributes()) {

                lengthOfSchema = lengthOfSchema + 2 + attribute.getAttributeName().length();
                raf.writeUTF(attribute.getAttributeName());

                if (attribute.isPrimaryKey()) {
                    raf.writeUTF("primarykey");
                    lengthOfSchema = lengthOfSchema + 2 + "primarykey".length();
                }
                if (attribute.isUnique()) {
                    raf.writeUTF("unique");
                    lengthOfSchema = lengthOfSchema + 2 + "unique".length();
                }
                if (attribute.isNotNull()) {
                    raf.writeUTF("notnull");
                    lengthOfSchema = lengthOfSchema + 2 + "notnull".length();
                }

                lengthOfSchema = lengthOfSchema + 2 + attribute.getTypeAsString().length();
                raf.writeUTF(attribute.getTypeAsString());

                switch (attribute.getTypeAsString()) {
                    case "integer":
                    case "boolean":
                    case "double":
                        break;
                    case "char":
                    case "varchar":
                        lengthOfSchema = lengthOfSchema + 4;
                        raf.writeInt(attribute.getLength());
                        break;
                }

            }
            raf.seek(positionOfLength);
            raf.writeInt(lengthOfSchema);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // create the initial db file with 0 pages
    public void initDB(Schema schema) {
        String fileName = this.catalog.getPath() + schema.getIndex() + "database.txt";

        File db = new File(fileName);
        try {
            db.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.writeInt(0);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // write a record to the file. raf should be at the correct spot already
    public void writeRecordToFile(Record record, RandomAccessFile raf) throws IOException {
        String fileName = this.catalog.getPath() + "database.txt";

        int recordSize = record.calculateBytes();
        ArrayList<RecordAttribute> recordData = record.getData();
        int numBits = recordData.size();
        int numBytes = (int) Math.ceil(numBits / 8.0);
        byte[] nullBitMap = new byte[numBytes];

        // null bit map.
        for (int i = 0; i < numBits; i++) {
            Object o = recordData.get(i);
            if (o == null) {
                nullBitMap[i / 8] |= (1 << (i % 8));
            }
        }

        // write record size
        raf.writeInt(recordSize);

        // write the null bit map
        raf.write(nullBitMap);

        for (RecordAttribute attribute : recordData) {
            if (attribute == null) {
                continue;
            } else {
                writeDataType(attribute, fileName, raf);
            }
        }
    }

    // given a page, it will write that page to the correct location in the file
    public void writePage(Page page) {
        String fileName = page.getFileName();

        int skipBytes = (page.getPageID() * this.catalog.getPageSize()) + 4;

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(fileName, "rw");

            int totalPages = raf.readInt();
            int oldTotal = totalPages;

            // if we are adding a new page then we need to increment the total pages
            if (page.getPageID() >= totalPages) {
                totalPages = page.getPageID() + 1;
                raf.seek(0);
                raf.writeInt(totalPages);
            }

            // if the page to insert is greater than the ammount of pages that are in the db
            // (ex. db have 0 pages but we want to write page 4)
            // create 4 blank pages and write page 4 after them
            int blankPageIndex = oldTotal;
            int blackPageByteLocation = (blankPageIndex * this.catalog.getPageSize()) + 4;
            raf.seek(blackPageByteLocation);
            while (blankPageIndex < page.getPageID()) {
                int junkSpace = Util.calculateJunkSpaceSize(new Page(blankPageIndex, new ArrayList<>(), catalog, fileName),
                        this.catalog.getPageSize());
                raf.writeInt(blankPageIndex);
                raf.writeInt(junkSpace);
                byte[] junk = new byte[junkSpace];
                raf.write(junk);
                blankPageIndex++;
            }

            raf.seek(skipBytes);

            // write the pageId, junkspace and record
            int junkSpace = Util.calculateJunkSpaceSize(page, this.catalog.getPageSize());
            raf.writeInt(page.getPageID());
            raf.writeInt(junkSpace);
            for (Record record : page.getRecords()) {
                writeRecordToFile(record, raf);
            }

            byte[] junk = new byte[junkSpace];
            raf.write(junk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // writes out a records attributes
    public void writeDataType(RecordAttribute attribute, String fileName, RandomAccessFile raf) throws IOException {
        if (attribute.getType() == int.class) {
            raf.writeInt((Integer) attribute.getAttribute());
        } else if (attribute.getType() == boolean.class) {
            raf.writeBoolean((Boolean) attribute.getAttribute());
        } else if (attribute.getType() == Character.class) {
            int charsToAdd = attribute.getCharLength() - ((String) attribute.getAttribute()).length();
            StringBuilder sb = new StringBuilder((String) attribute.getAttribute());
            for (int i = 0; i < charsToAdd; i++) {
                sb.append("-");
            }
            raf.writeUTF(sb.toString());
            raf.writeInt(charsToAdd);
        } else if (attribute.getType() == String.class) {
            raf.writeUTF((String) attribute.getAttribute());
        } else if (attribute.getType() == double.class) {
            raf.writeDouble((Double) attribute.getAttribute());
        }
    }
}
