import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BinaryWriter {
    
    public BinaryWriter() {
        return;
    }

    public void writeToFile(ArrayList<Object> data, String fileName) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName))) {
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
