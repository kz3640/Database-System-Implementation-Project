package Record;

import java.util.ArrayList;

import Util.Util;

public class Record {
    private ArrayList<RecordAttribute> data;
    private String tableName;

    public Record(ArrayList<RecordAttribute> data, String tableName) {
        this.data = data;
        this.tableName = tableName;
    }

    public Record(Record otherRecord) {
        this.data = new ArrayList<>(otherRecord.getData().size());
        for (RecordAttribute attribute : otherRecord.getData()) {
            this.data.add(new RecordAttribute(attribute.getType(), attribute.getAttribute(), attribute.getCharLength()));
        }
        this.tableName = otherRecord.getTableName();
    }

    public String getTableName() {
        return tableName;
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
            System.out.print(col.getAttribute());
            System.out.print(" | ");
        }
        // System.out.println("");

        // debugging
        // for (RecordAttribute col : this.data) {
        // System.out.print(" | ");
        // System.out.print(col.getAttribute());
        // }
        // System.out.println("");
    }

    // calculate the total amount of bytes that the record takes up
    public int calculateBytes() {
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
                size += attribute.getAttribute() == null ? 0
                        : ((String) attribute.getAttribute()).getBytes().length + 2;
            } else if (attribute.getType() == double.class) {
                size += 8;
            }
        }
        return size;
    }
}
