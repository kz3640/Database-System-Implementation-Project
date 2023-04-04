package Catalog;

public class Char implements SchemaAttribute {
    private String attributeName;
    private int length;
    private boolean isPrimaryKey;
    private boolean notNull;
    private boolean isUnique;
    private String defaultValue;

    public Char(String attributeName, int length, boolean isPrimaryKey, boolean notNull, boolean isUnique, String defaultValue) {
        this.attributeName = attributeName;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.notNull = notNull;
        this.isUnique = isUnique;
        this.defaultValue = defaultValue;
    }

    public int getLength() {
        return length;
    }

    public Class<Character> getType() {
        return Character.class;
    }

    public String getTypeAsString() {
        return "char";
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public Object getDefault() {
        return this.defaultValue;
    }

    public SchemaAttribute updateNameForJoin(String tableName) {
        return new Char(tableName + "." + attributeName, length, isPrimaryKey, notNull, isUnique, defaultValue);
    }
}
