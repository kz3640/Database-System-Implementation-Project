package IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import Buffer.Page;
import Record.Record;
import Record.RecordAttribute;
import Schema.Schema;
import Util.Util;

public class BinaryWriter {
    private Schema schema;

    public BinaryWriter(Schema schema) {
        this.schema = schema;
        return;
    }

    public void writeRecordToFile(Record record, RandomAccessFile raf) throws IOException {
        String fileName = this.schema.getPath() + "database.txt";
        
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

    public void insertNewPage(Page page) throws IOException {
        String fileName = this.schema.getPath() + "database.txt";
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");

        int skipBytes = ((page.getPageID()) * this.schema.getPageSize()) + 4;

        int totalPages = raf.readInt();
        totalPages++;
        raf.seek(0);
        raf.writeInt(totalPages);

        // add at end of file
        if (skipBytes >= raf.length()) {
            writePage(page);
            raf.close();
            return;
        }

        raf.seek(skipBytes);

        int bytesToRead = (int) (raf.length() - raf.getFilePointer());
        byte[] buffer = new byte[bytesToRead];
        raf.read(buffer, 0, bytesToRead);
        writePage(page);
        raf.write(buffer);
        raf.close();

        increaseAllPageIds(page.getPageID());
        return;
    }

    private void increaseAllPageIds(int pageIndex) throws IOException {
        String fileName = this.schema.getPath() + "database.txt";
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        int totalPages = raf.readInt();

        int pageToChange = pageIndex;
        while (pageToChange < totalPages) {
            int skipBytes = (pageToChange * this.schema.getPageSize()) + 4;
            raf.seek(skipBytes);
            raf.writeInt(pageToChange);
            pageToChange++;
        }
        raf.close();
    }

    public int getTotalPages() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.schema.getPath() + "database.txt", "rw");
        int totalPages = raf.readInt();
        raf.close();
        return totalPages;
    }

    public void initDB() throws IOException {
        File db = new File(this.schema.getPath() + "database.txt");
        db.createNewFile();
        RandomAccessFile raf = new RandomAccessFile(this.schema.getPath() + "database.txt", "rw");
        raf.writeInt(0);
        raf.close();
    }

    public void writePage(Page page) throws FileNotFoundException, IOException {

        String fileName = this.schema.getPath() + "database.txt";

        int skipBytes = (page.getPageID() * this.schema.getPageSize()) + 4;

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        int totalPages = raf.readInt();

        // if we are adding a new page then we need to increment the total pages
        if (page.getPageID() >= totalPages) {
            totalPages++;
            raf.seek(0);
            raf.writeInt(totalPages);
        }

        raf.seek(skipBytes);

        int junkSpace = Util.calculateJunkSpaceSize(page, this.schema.getPageSize());
        raf.writeInt(page.getPageID());
        raf.writeInt(junkSpace);
        for (Record record : page.getRecords()) {
            writeRecordToFile(record, raf);
        }

        byte[] junk = new byte[junkSpace];
        raf.write(junk);
    }

    public void writeSchemaToFile(ArrayList<Object> data) throws IOException {
        String fileName = this.schema.getPath() + "catalog.txt";
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        for (Object o : data) {
            writeSchemaDataType(o, fileName, raf);
        }
    }

    public void writeSchemaDataType(Object o, String fileName, RandomAccessFile raf) throws IOException {
        if (o instanceof Integer) {
            raf.writeInt((Integer) o);
        } else if (o instanceof Boolean) {
            raf.writeBoolean((Boolean) o);
        } else if (o instanceof Character) {
            raf.writeChar((Character) o);
        } else if (o instanceof String) {
            raf.writeUTF((String) o);
        } else if (o instanceof Double) {
            raf.writeDouble((Double) o);
        }
    }

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
