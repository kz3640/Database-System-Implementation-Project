package Schema;

public class Varchar implements SchemaAttribute {
    private String attributeName;
    private int length;
    private boolean isPrimaryKey;;
    private boolean notNull;;

    public Varchar(String attributeName, int length, boolean isPrimaryKey, boolean notNull) {
        this.attributeName = attributeName;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
        this.notNull = notNull;
    }

    public int getLength() {
        return length;
    }

    public Class<String> getType() {
        return String.class;
    }

    public Character getLetter() {
        return 'v';
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
}
