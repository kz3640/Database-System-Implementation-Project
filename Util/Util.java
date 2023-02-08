package Util;

import Record.Record;
import Buffer.Page;

public class Util {
    
    public static int calculatePageSize(Page page) {
        int totalSize = 0;

        for (Record record : page.getRecords()) {
            totalSize += calculateRecordSize(record);
        }
        totalSize += 4; // int id
        totalSize += 4; // int junkSize
        return totalSize;
    }

    public static int calculateRecordSize(Record data) {
        return data.calculateBytes() + 4;
    }

    public static int calculateJunkSpaceSize(Page page, int pageSize) {
        return pageSize - calculatePageSize(page);
    }
}
