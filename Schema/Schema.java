package Schema;

import java.util.ArrayList;

public class Schema {
    private String tableName;
    private ArrayList<SchemaAttribute> attributes;
    private int indexOfPrimaryKey;


    public Schema(String tableName, ArrayList<SchemaAttribute> attributes) {
        this.tableName = tableName;
        this.attributes = attributes;
        for (SchemaAttribute schemaAttribute : attributes) {
            if (schemaAttribute.isPrimaryKey()) {
                this.indexOfPrimaryKey = attributes.indexOf(schemaAttribute);
                break;
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<SchemaAttribute> getAttributes() {
        return attributes;
    }

    public int getIndexOfPrimaryKey() {
        return this.indexOfPrimaryKey;
    }

    public void printTable() {
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
