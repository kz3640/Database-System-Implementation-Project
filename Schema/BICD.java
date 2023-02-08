package Schema;

public class BICD implements SchemaAttribute{
    private String attributeName;
    private Character character;
    private boolean isPrimaryKey;
    private boolean notNull;

    public BICD(String attributeName, Character character, boolean isPrimaryKey, boolean notNull) {
        this.attributeName = attributeName;
        this.character = character;
        this.isPrimaryKey = isPrimaryKey;
        this.notNull = notNull;
    }

    public Class<?> getType() {
        switch (character) {
            case 'b':
                return boolean.class;
            case 'i':
                return int.class;
            case 'd':
                return double.class;
            default:
                return null;
        }
    }

    public int getLength() {
        return -1;
    }

    public Character getLetter() {
        return this.character;
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
