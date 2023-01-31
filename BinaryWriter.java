import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BinaryWriter {

    public BinaryWriter() {
        return;
    }

    public void writeToFile(ArrayList<Object> data, String fileName, Integer recordSize) throws IOException {

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName, true));
                DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {

            // read until end of tile
            // need to change this to read until correct spot to insert
            while (true && recordSize != null) {
                try {
                    int prevRecordSize = dis.readInt();
                    dis.skip(prevRecordSize);
                } catch (java.io.EOFException e) {
                    break;
                }
            }
            // write record size
            if (recordSize != null) {
                dos.writeInt(recordSize);
            }
            for (Object o : data) {
                if (o instanceof Integer) {
                    dos.writeInt((Integer) o);
                } else if (o instanceof Boolean) {
                    dos.writeBoolean((Boolean) o);
                } else if (o instanceof Character) {
                    dos.writeChar((Character) o);
                } else if (o instanceof String) {
                    dos.writeUTF((String) o);
                } else if (o instanceof Double) {
                    dos.writeDouble((Double) o);
                }
            }
        }
    }

    public int calculateBytes(ArrayList<Object> data) {
        int size = 0;
        for (Object o : data) {
            if (o instanceof Integer) {
                size += 4;
            } else if (o instanceof Boolean) {
                size += 1;
            } else if (o instanceof Character) {
                size += 2;
            } else if (o instanceof String) {
                size += ((String) o).getBytes().length;
            } else if (o instanceof Double) {
                size += 8;
            }
        }
        return size;
    }

    public ArrayList<Object> addDataToArray(String[] input) {
        ArrayList<Object> data = new ArrayList<Object>();
        for (String s : input) {
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
