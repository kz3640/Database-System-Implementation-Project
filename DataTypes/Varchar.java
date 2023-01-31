package DataTypes;

public class Varchar implements SchemaDataType {
    private int length;

    public Varchar(int length) {
        this.length = length;
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
}
