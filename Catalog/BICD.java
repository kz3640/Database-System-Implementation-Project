package Catalog;

public class BICD implements SchemaAttribute{
    private String attributeName;
    private String typeAsString;
    private boolean isPrimaryKey;
    private boolean notNull;

    public BICD(String attributeName, String typeAsString, boolean isPrimaryKey, boolean notNull) {
        this.attributeName = attributeName;
        this.typeAsString = typeAsString;
        this.isPrimaryKey = isPrimaryKey;
        this.notNull = notNull;
    }

    public Class<?> getType() {
        switch (typeAsString) {
            case "boolean":
                return boolean.class;
            case "integer":
                return int.class;
            case "double":
                return double.class;
            default:
                return null;
        }
    }

    public int getLength() {
        return -1;
    }

    public String getTypeAsString() {
        return this.typeAsString;
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public boolean isNotNull() {
        return this.notNull;
    }
}
