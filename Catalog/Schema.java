package Catalog;

import java.io.File;
import java.util.ArrayList;

import Record.RecordAttribute;
import Tree.BPlusTree;
import Record.Record;

public class Schema {
    private ArrayList<SchemaAttribute> attributes;
    private String tableName;
    private String index;
    private Catalog catalog;
    private int indexOfPrimaryKey;
    private BPlusTree bpt;

    public Schema(String tableName, ArrayList<SchemaAttribute> attributes, Catalog catalog, BPlusTree bpt) {
        this.tableName = tableName;
        this.catalog = catalog;
        this.attributes = attributes;
        this.bpt = bpt;

        if (attributes != null) {
            for (SchemaAttribute schemaAttribute : attributes) {
                if (schemaAttribute.isPrimaryKey()) {
                    this.indexOfPrimaryKey = attributes.indexOf(schemaAttribute);
                    break;
                }
            }
        }
        return;
    }

    public void addBPT(BPlusTree bpt) {
        this.bpt = bpt;
    }

    public BPlusTree getBpt() {
        return bpt;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public ArrayList<SchemaAttribute> getAttributes() {
        return attributes;
    }

    public String getTableName() {
        return tableName;
    }

    public void printSchema() {
        System.out.println(this.tableName);
        for (SchemaAttribute attribute : this.attributes) {
            String attributeLine = "    " + attribute.getAttributeName();
            attributeLine = attributeLine + " " + attribute.getTypeAsString();
            if (attribute.getLength() > 0) {
                attributeLine = attributeLine + " " + attribute.getLength() + " ";
            }
            if (attribute.isPrimaryKey()) {
                attributeLine += " primarykey";
            }
            if (attribute.isNotNull()) {
                attributeLine += " not null";
            }
            if (attribute.isUnique()) {
                attributeLine += " unique";
            }
            if (attribute.getDefault() != null) {
                attributeLine += " default: " + attribute.getDefault();
            }
            System.out.println(attributeLine);
        }
    }

    public String getFileName() {
        return this.catalog.getPath() + this.index + "database.txt";
    }

    public String getBPlusTreeFileName() {
        return this.catalog.getPath() + this.index + "bplusTree.txt";
    }

    public int getIndexOfPrimaryKey() {
        return this.indexOfPrimaryKey;
    }

    public ArrayList<Integer> getIndexesOfUniqueValues() {
        ArrayList<Integer> indexesOfUniqueValues = new ArrayList<>();

        int i = 0;
        for (SchemaAttribute attribute : this.attributes) {
            if (attribute.isUnique())
                indexesOfUniqueValues.add(i);
            i++;
        }

        return indexesOfUniqueValues;
    }

    public boolean doesRecordFollowSchema(Record record) {
        ArrayList<SchemaAttribute> schemaAttributes = this.getAttributes();

        ArrayList<RecordAttribute> recordAttributes = record.getData();
        if (recordAttributes.size() != schemaAttributes.size()) {
            System.out.println("---ERROR---");
            System.out.println("Invalid record attribute ammount");
            return false;
        }

        for (int index = 0; index < recordAttributes.size(); index++) {

            if (schemaAttributes.get(index).isPrimaryKey() && recordAttributes.get(index).getAttribute() == null) {
                System.out.println("---ERROR---");
                System.out.println("Primary key attribute can't be null");
                return false;
            }
            if (schemaAttributes.get(index).isNotNull() && recordAttributes.get(index).getAttribute() == null) {
                System.out.println("---ERROR---");
                System.out.println("Record has null value but attribute must be not null");
                return false;
            }
            if (!schemaAttributes.get(index).isNotNull() && recordAttributes.get(index).getAttribute() == null) {
                continue;
            }
            switch (schemaAttributes.get(index).getTypeAsString()) {
                case "integer":
                    if (!(recordAttributes.get(index).getType() == int.class)) {
                        System.out.println("---ERROR---");
                        System.out.println("Record does not fit the scehma");
                        System.out.println(recordAttributes.get(index).getType().getSimpleName() + " != "  + int.class.getSimpleName());
                        return false;
                    }
                    break;
                case "varchar":
                    if (!(recordAttributes.get(index).getType() == String.class)) {
                        System.out.println("---ERROR---");
                        System.out.println("Record does not fit the scehma");
                        System.out.println(recordAttributes.get(index).getType().getSimpleName() + " != varchar");
                        return false;
                    } else {
                        String recordString = (String) recordAttributes.get(index).getAttribute();
                        if (schemaAttributes.get(index).getLength() < recordString.length()) {
                            System.out.println("---ERROR---");
                            System.out.println("Varchar of length " + recordString.length()
                                    + " is too large for length " + schemaAttributes.get(index).getLength());
                            return false;
                        }
                    }
                    break;
                case "char":
                    if (!(recordAttributes.get(index).getType() == Character.class)) {
                        System.out.println("---ERROR---");
                        System.out.println("Record does not fit the scehma");
                        System.out.println(recordAttributes.get(index).getType().getSimpleName() + " != char");
                        return false;
                    } else {
                        String recordString = (String) recordAttributes.get(index).getAttribute();
                        if (schemaAttributes.get(index).getLength() < recordString.length()) {
                            System.out.println("---ERROR---");
                            System.out.println("Char of length " + recordString.length()
                                    + " is too large for length " + schemaAttributes.get(index).getLength());
                            return false;
                        }
                    }
                    break;
                case "double":
                    if (!(recordAttributes.get(index).getType() == double.class)) {
                        System.out.println("---ERROR---");
                        System.out.println(recordAttributes.get(index).getType().getSimpleName() + " != "  + double.class.getSimpleName());
                        System.out.println("Record does not fit the scehma");
                        return false;
                    }
                    break;
                case "boolean":
                    if (!(recordAttributes.get(index).getType() == boolean.class)) {
                        System.out.println("---ERROR---");
                        System.out.println("Record does not fit the scehma");
                        System.out.println(recordAttributes.get(index).getType().getSimpleName() + " != "  + boolean.class.getSimpleName());
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public void remove() {
        String fileName = this.catalog.getPath() + this.index + "database.txt";
        File file = new File(fileName);
        file.delete();
    }

    public void convertToTemp() {
        String oldFileName = this.catalog.getPath() + this.index + "database.txt";
        this.index = "-1";
        String newFileName = this.catalog.getPath() + this.index + "database.txt";
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        oldFile.renameTo(newFile);
    }

    public boolean isAtributeInSchema(String val) {
        for (SchemaAttribute schemaAttribute : attributes) {
            if (schemaAttribute.getAttributeName().equals(val)) {
                return true;
            }
        }
        return false;
    }

    public int getIndexOfAttributeName(String attributeOrValue) {

        int i = 0;
        for (SchemaAttribute schemaAttribute : attributes) {
            if (schemaAttribute.getAttributeName().equals(attributeOrValue)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    public ArrayList<SchemaAttribute> getJoinedAttributeName() {
        ArrayList<SchemaAttribute> newAttribtues = new ArrayList<>();

        for (SchemaAttribute schemaAttribute : attributes) {

            SchemaAttribute newAttribtue = schemaAttribute.updateNameForJoin(this.getTableName());
            newAttribtues.add(newAttribtue);
        }

        return newAttribtues;
    }
}
