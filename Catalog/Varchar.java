package Catalog;

public class Varchar implements SchemaAttribute {
    private String attributeName;
    private int length;
    private boolean isPrimaryKey;
    private boolean notNull;
    private boolean isUnique;

    public Varchar(String attributeName, int length, boolean isPrimaryKey, boolean notNull, boolean isUnique) {
        this.attributeName = attributeName;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.notNull = notNull;
        this.isUnique = isUnique;
    }

    public int getLength() {
        return length;
    }

    public Class<String> getType() {
        return String.class;
    }

    public String getTypeAsString() {
        return "varchar";
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

    public boolean isUnique() {
        return this.isUnique;
    }
}
