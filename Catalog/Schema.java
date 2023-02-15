package Catalog;

import java.util.ArrayList;

import Record.RecordAttribute;
import Record.Record;

public class Schema {
    private ArrayList<SchemaAttribute> attributes;
    private String tableName;
    private String index;
    private Catalog catalog;
    private int indexOfPrimaryKey;

    public Schema(String tableName, ArrayList<SchemaAttribute> attributes, Catalog catalog) {
        this.tableName = tableName;
        this.catalog = catalog;
        this.attributes = attributes;

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
            if (attribute.isPrimaryKey()) {
                attributeLine += " primarykey";
            }
            System.out.println(attributeLine);
        }
    }

    public String getFileName() {
        return this.catalog.getPath() + this.index + "database.txt";
    }

    public int getIndexOfPrimaryKey() {
        return this.indexOfPrimaryKey;
    }

    public boolean doesRecordFollowSchema(Record record) {
        ArrayList<SchemaAttribute> schemaAttributes = this.getAttributes();

        ArrayList<RecordAttribute> recordAttributes = record.getData();
        if (recordAttributes.size() != schemaAttributes.size()) {
            return false;
        }

        for (int index = 0; index < recordAttributes.size(); index++) {
            if (schemaAttributes.get(index).isNotNull() && recordAttributes.get(index) == null) {
                return false;
            }
            if (!schemaAttributes.get(index).isNotNull() && recordAttributes.get(index) == null) {
                continue;
            }
            switch (schemaAttributes.get(index).getTypeAsString()) {
                case "integer":
                    if (!(recordAttributes.get(index).getType() == int.class)) {
                        return false;
                    }
                    break;
                case "varchar":
                    if (!(recordAttributes.get(index).getType() == String.class)) {
                        return false;
                    }
                    break;
                case "char":
                    if (!(recordAttributes.get(index).getType() == Character.class)) {
                        return false;
                    }
                    break;
                case "double":
                    if (!(recordAttributes.get(index).getType() == double.class)) {
                        return false;
                    }
                    break;
                case "boolean":
                    if (!(recordAttributes.get(index).getType() == boolean.class)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}
