package IO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Tree.BPlusTree;
import Tree.BPlusTree.NodeInfo;
import Catalog.Catalog;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Util.Util;

public class BinaryWriter {
    protected Catalog catalog;
    protected String dbName = "database.txt";

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
            raf.close();
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
            raf.seek(raf.length());
            addSchemaToPosition(raf, raf.getFilePointer(), schema);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // create the initial db file with 0 pages
    public void initDB(Schema schema) {
        String fileName = this.catalog.getPath() + schema.getIndex() + dbName;

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
        String fileName = this.catalog.getPath() + dbName;

        int recordSize = record.calculateBytes();
        ArrayList<RecordAttribute> recordData = record.getData();
        int numBits = recordData.size();
        int numBytes = (int) Math.ceil(numBits / 8.0);
        byte[] nullBitMap = new byte[numBytes];

        // null bit map.
        for (int i = 0; i < numBits; i++) {
            RecordAttribute o = recordData.get(i);
            if (o.getAttribute() == null) {
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

    public void writeBPlusTree(Schema schema) {
        BPlusTree bpt = schema.getBpt();

        ArrayList<NodeInfo> leafs = bpt.getAllLeafs();

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(schema.getBPlusTreeFileName(), "rw");
            raf.writeInt(leafs.size());

            for (NodeInfo nodeInfo : leafs) {
                if (bpt.getType().equals("integer")) {
                    raf.writeInt((int) nodeInfo.getKey());
                } else if (bpt.getType().equals("stirng")) {
                    raf.writeUTF((String) nodeInfo.getKey());
                } else if (bpt.getType().equals("double")) {
                    raf.writeDouble((Double) nodeInfo.getKey());
                } else if (bpt.getType().equals("boolean")) {
                    raf.writeBoolean((Boolean) nodeInfo.getKey());
                }
                raf.writeInt(nodeInfo.getPageInfo().pageIndex);
                raf.writeInt(nodeInfo.getPageInfo().positionIndex);
            }

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                int junkSpace = Util.calculateJunkSpaceSize(
                        new Page(blankPageIndex, new ArrayList<>(), catalog, fileName),
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
            raf.close();
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

    public void deleteFile(Schema schema) {
        String fileName = this.catalog.getPath() + schema.getIndex() + dbName;
        File file = new File(fileName);
        file.delete();
    }

    public void alterTable(Schema schema) {
        String tableNameToDelete = schema.getTableName();
        String fileName = this.catalog.getPath() + "catalog.txt";

        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            raf.readInt(); // pageSize, we ignore
            int totalTables = raf.readInt();

            int tableIdToRead = 0;
            // loop through each table
            while (tableIdToRead < totalTables) {

                long pointer = raf.getFilePointer();
                int sizeOfSchema = raf.readInt();
                String tableName = raf.readUTF();
                int remainingBytesInSchema = sizeOfSchema - tableName.length() - 2; // need to add the length of the
                                                                                    // string back
                byte[] bytesToSkipOver = new byte[remainingBytesInSchema];

                if (tableName.equals(tableNameToDelete)) {
                    int schemaSize = sizeOfSchema + 4;
                    // store all following schemas
                    raf.seek(pointer + schemaSize);
                    byte[] remainingBytes = new byte[(int) ((int) raf.length() - schemaSize - pointer)];
                    raf.read(remainingBytes);

                    addSchemaToPosition(raf, pointer, schema);

                    raf.seek(pointer);
                    sizeOfSchema = raf.readInt();
                    bytesToSkipOver = new byte[sizeOfSchema];
                    raf.read(bytesToSkipOver);

                    raf.write(remainingBytes);
                    raf.setLength(raf.getFilePointer());
                    return;
                }

                raf.read(bytesToSkipOver);
                tableIdToRead++;
            }
            raf.close();

            System.out.println("Not Found");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSchemaToPosition(RandomAccessFile raf, long position, Schema schema) throws IOException {
        int lengthOfSchema = 0;
        raf.seek(position);
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
    }

    public void removeTableFromCatalog(Schema schema) {
        String tableNameToDelete = schema.getTableName();
        String fileName = this.catalog.getPath() + "catalog.txt";

        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            raf.readInt(); // pageSize, we ignore
            int totalTables = raf.readInt();

            int tableIdToRead = 0;
            // loop through each table
            while (tableIdToRead < totalTables) {

                long pointer = raf.getFilePointer();
                int sizeOfSchema = raf.readInt();
                String tableName = raf.readUTF();
                int remainingBytesInSchema = sizeOfSchema - tableName.length() - 2; // need to add the length of the
                                                                                    // string back
                byte[] bytesToSkipOver = new byte[remainingBytesInSchema];

                if (tableName.equals(tableNameToDelete)) {
                    int schemaSize = sizeOfSchema + 4;

                    // remove schema from catalog file
                    removeBytesFromFile(raf, fileName, pointer, schemaSize);

                    // update table count
                    raf.seek(4);
                    raf.writeInt(totalTables - 1);
                    catalog.dropTable(schema); // remove it from the catalog

                    // need to rename every file that comes after the one we deleted.
                    renameFilesAfter(tableIdToRead, totalTables);
                    this.catalog.balanceTableIndex(tableIdToRead);

                    return;
                }

                raf.read(bytesToSkipOver);
                tableIdToRead++;
            }
            raf.close();

            System.out.println("Not Found");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameFilesAfter(int index, int numFiles) {
        while (index < numFiles) {
            String oldFileName = this.catalog.getPath() + index + dbName;
            String newFileName = this.catalog.getPath() + (index - 1) + dbName;
            File oldFile = new File(oldFileName);
            File newFile = new File(newFileName);
            oldFile.renameTo(newFile);
            index++;
        }

    }

    private void removeBytesFromFile(RandomAccessFile raf, String filePath, long position, int numBytesToRemove)
            throws IOException {

        if (position + numBytesToRemove == raf.length()) {
            raf.setLength(position);
            return;
        }
        raf.seek(position + numBytesToRemove);

        // shift the remaining bytes to the left
        byte[] remainingBytes = new byte[(int) ((int) raf.length() - numBytesToRemove - position)];
        raf.read(remainingBytes);
        raf.seek(position);
        raf.write(remainingBytes);

        // truncate the file to remove the bytes we don't want
        raf.setLength(raf.getFilePointer());
    }

    public void updatePageTotal(Schema schema, int pagesLeft) {
        String fileName = this.catalog.getPath() + schema.getIndex() + dbName;

        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            raf.seek(0);
            raf.writeInt(pagesLeft);

            int skipBytes = ((pagesLeft) * this.catalog.getPageSize()) + 4;
            raf.seek(skipBytes);
            raf.setLength(raf.getFilePointer());
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
