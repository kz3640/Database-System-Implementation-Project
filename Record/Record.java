package Record;

import java.util.ArrayList;

import Util.Util;

public class Record {
    private ArrayList<RecordAttribute> data;

    public Record(ArrayList<RecordAttribute> data) {
        this.data = data;
    }

    public ArrayList<RecordAttribute> getData() {
        return data;
    }

    public int calculateRecordSize() {
        return Util.calculateRecordSize(this);
    }

    public void addAttributeToData(RecordAttribute recordAttribute) {
        this.data.add(recordAttribute);
    }

    public void printRecord() {
        for (RecordAttribute col : this.data) {
            System.out.print(" | ");
            System.out.print(col.getAttribute());
        }
        System.out.println("");
    }

    public int calculateBytes() {
        // data.getData();
        int size = 0;
        int numOfBits = data.size();
        int numOfBytes = (int) Math.ceil((double) numOfBits / 8);
        size += numOfBytes;
        for (RecordAttribute attribute : data) {
            if (attribute == null) {
                continue;
            }
            if (attribute.getType() == int.class) {
                size += 4;
            } else if (attribute.getType() == boolean.class) {
                size += 1;
            } else if (attribute.getType() == Character.class) {
                size += attribute.getCharLength() + 2 + 4;
            } else if (attribute.getType() == String.class) {
                size += ((String) attribute.getAttribute()).getBytes().length + 2;
            } else if (attribute.getType() == double.class) {
                size += 8;
            }
        }
        return size;
    }
}
