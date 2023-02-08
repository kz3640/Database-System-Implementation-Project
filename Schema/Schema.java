package Schema;

import java.util.ArrayList;

public class Schema {
    private String tableName;
    private String path;
    private int pageSize;
    private ArrayList<SchemaAttribute> attributes;
    private int indexOfPrimaryKey;


    public Schema(String tableName, String path, int pageSize, ArrayList<SchemaAttribute> attributes) {
        this.tableName = tableName;
        this.pageSize = pageSize;
        this.attributes = attributes;
        this.path = path;

        if (attributes != null) {
            for (SchemaAttribute schemaAttribute : attributes) {
                if (schemaAttribute.isPrimaryKey()) {
                    this.indexOfPrimaryKey = attributes.indexOf(schemaAttribute);
                    break;
                }
            }
        }
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setAttributes(ArrayList<SchemaAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<SchemaAttribute> getAttributes() {
        if (attributes == null) {
            System.out.println("There is no table at this path. Create one before trying to insert.");
        }
        return attributes;
    }

    public String getPath() {
        return path;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getIndexOfPrimaryKey() {
        return this.indexOfPrimaryKey;
    }

    public void printTable() {
        System.out.println(this.pageSize);
        System.out.println(this.tableName);
        for (SchemaAttribute schemaAttribute : attributes) {

            String attributeLine = schemaAttribute.getAttributeName();
            attributeLine = attributeLine + " " + schemaAttribute.getType().getSimpleName();
            if (schemaAttribute.isPrimaryKey()) {
                attributeLine += " primarykey";
            }
            System.out.println(attributeLine);
        }
    }
}
