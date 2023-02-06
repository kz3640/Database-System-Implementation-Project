import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Schema.Schema;

public class BinaryWriter {

    public BinaryWriter() {
        return;
    }

    // read until end of tile
    // need to change this to read until correct spot to insert
    // while (true) {
    // try {
    // int prevRecordSize = dis.readInt();
    // dis.skip(prevRecordSize);
    // } catch (java.io.EOFException e) {
    // break;
    // }
    // }

    public void writeRecordToFile(ArrayList<Object> data, String fileName, RandomAccessFile raf) throws IOException {

        int recordSize = calculateBytes(data);
        int numBits = data.size();
        int numBytes = (int) Math.ceil(numBits / 8.0);
        byte[] nullBitMap = new byte[numBytes];

        // null bit map.
        for (int i = 0; i < numBits; i++) {
            Object o = data.get(i);
            if (o == null) {
                nullBitMap[i / 8] |= (1 << (i % 8));
            }
        }

        // write record size
        raf.writeInt(recordSize);

        // write the null bit map
        raf.write(nullBitMap);
        for (Object o : data) {
            if (o == null) {
                continue;
            } else {
                writeDataType(o, fileName, raf);
            }
        }
    }

    public int calculatePageSize(ArrayList<ArrayList<Object>> allRecords) {
        int totalSize = 0;
        for (ArrayList<Object> record : allRecords) {
            totalSize += calculateRecordSize(record);
        }
        totalSize += 4; // int id
        totalSize += 4; // int junkSize
        return totalSize;
    }

    public int calculateRecordSize(ArrayList<Object> data) {
        return this.calculateBytes(data) + 4;
    }

    public int calculateJunkSpaceSize(ArrayList<ArrayList<Object>> allRecords, int pageSize) {
        return pageSize - calculatePageSize(allRecords);
    }

    public void writePage(ArrayList<ArrayList<Object>> allRecords, String fileName, Schema schema, int pageNumber,
            int pageSize) throws FileNotFoundException, IOException {

        int segmentSize = 2;
        int numSegments = (int) Math.ceil(allRecords.size() / (double) segmentSize);
        ArrayList<ArrayList<ArrayList<Object>>> segments = new ArrayList<>();

        for (int i = 0; i < numSegments; i++) {
            int startIndex = i * segmentSize;
            int endIndex = Math.min((i + 1) * segmentSize, allRecords.size());
            ArrayList<ArrayList<Object>> segment = new ArrayList<>(allRecords.subList(startIndex, endIndex));
            segments.add(segment);
        }
        int skipBytes = (pageNumber * pageSize) + 4;

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        raf.seek(skipBytes);

        writePageToFile(raf, segments.get(0), fileName, schema);
    }

    public void writeAll(ArrayList<ArrayList<Object>> allRecords, String fileName, Schema schema) throws IOException {
        int segmentSize = 2;
        int numSegments = (int) Math.ceil(allRecords.size() / (double) segmentSize);
        ArrayList<ArrayList<ArrayList<Object>>> segments = new ArrayList<>();

        for (int i = 0; i < numSegments; i++) {
            int startIndex = i * segmentSize;
            int endIndex = Math.min((i + 1) * segmentSize, allRecords.size());
            ArrayList<ArrayList<Object>> segment = new ArrayList<>(allRecords.subList(startIndex, endIndex));
            segments.add(segment);
        }

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName, false))) {
            raf.writeInt(numSegments);
            for (ArrayList<ArrayList<Object>> segment : segments) {
                writePageToFile(raf, segment, fileName, schema);
            }
        }
    }

    public void writePageToFile(RandomAccessFile raf, ArrayList<ArrayList<Object>> allRecords, String fileName,
            Schema schema)
            throws IOException {
        int pageSize = 100;
        int junkSpace = calculateJunkSpaceSize(allRecords, pageSize);
        raf.writeInt(Integer.parseInt("1"));
        raf.writeInt(junkSpace);
        for (ArrayList<Object> record : allRecords) {
            writeRecordToFile(record, fileName, raf);
        }

        byte[] junk = new byte[junkSpace];
        raf.write(junk);
    }

    public void writeSchemaToFile(ArrayList<Object> data, String fileName) throws IOException {
        // try (DataOutputStream dos = new DataOutputStream(new
        // FileOutputStream(fileName, true));
        // DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        for (Object o : data) {
            writeDataType(o, fileName, raf);
        }
        // }
    }

    public void writeDataType(Object o, String fileName, RandomAccessFile raf) throws IOException {
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

    public int calculateBytes(ArrayList<Object> data) {
        int size = 0;
        int numOfBits = data.size();
        int numOfBytes = (int) Math.ceil((double) numOfBits / 8);
        size += numOfBytes;
        for (Object o : data) {
            if (o == null) {
                continue;
            }
            if (o instanceof Integer) {
                size += 4;
            } else if (o instanceof Boolean) {
                size += 1;
            } else if (o instanceof Character) {
                size += 2;
            } else if (o instanceof String) {
                size += ((String) o).getBytes().length + 2;
            } else if (o instanceof Double) {
                size += 8;
            }
        }
        return size;
    }

    public ArrayList<Object> addDataToArray(String[] input) {
        ArrayList<Object> data = new ArrayList<Object>();
        for (String s : input) {
            if (s.equals("null")) {
                data.add(null);
                continue;
            }
            try {
                int i = Integer.parseInt(s);
                data.add(i);
            } catch (NumberFormatException e) {
                String lowerString = s.toLowerCase();
                if (lowerString.equals("true") || lowerString.equals("false")) {
                    boolean b = Boolean.parseBoolean(s);
                    data.add(b);
                } else {
                    try {
                        double d = Double.parseDouble(s);
                        data.add(d);
                    } catch (NumberFormatException exx) {
                        if (s.length() == 1) {
                            data.add(s.charAt(0));
                        } else {
                            data.add(s);
                        }
                    }
                }
            }
        }
        return data;
    }
}
