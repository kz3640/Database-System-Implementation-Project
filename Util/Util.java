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

    public static Object convertToType(String attributeType, String attributeValue) {
        if (attributeValue.equals("null")) {
            return null;
        }
        switch (attributeType) {
            case "integer":
                try {
                    return Integer.parseInt(attributeValue);
                } catch (NumberFormatException e) {
                }
            case "double":
                try {
                    return Double.parseDouble(attributeValue);
                } catch (NumberFormatException e2) {
                }
            case "boolean":
                return Boolean.parseBoolean(attributeValue);
            default:
                return attributeValue;
        }
    }

    public static boolean doesStringFitType(String attributeType, String attributeValue) {
        if (attributeType.equals("null")) {
            return true;
        }
        switch (attributeType) {
            case "integer":
                try {
                    Integer.parseInt(attributeValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "double":
                try {
                    Double.parseDouble(attributeValue);
                    return false;
                } catch (NumberFormatException e2) {
                    return false;
                }
            case "boolean":
                if (attributeValue.equals("true") || attributeValue.equals("false")) {
                    return true;
                }
                break;
            default:
                if (!(attributeValue.startsWith("\"") && attributeValue.endsWith("\""))) {
                    return false;
                }
                if (attributeType.matches("char\\([0-9]+\\)") || attributeType.matches("varchar\\([0-9]+\\)")) {
                    String trimmedStr = attributeValue.substring(1, attributeValue.length() - 1);
                    if (!trimmedStr.contains("\""))
                        return true;
                }
        }
        return false;
    }
}
