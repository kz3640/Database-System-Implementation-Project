package DataTypes;

public class OtherDataType implements SchemaDataType{
    private Character character;

    public OtherDataType(Character character) {
        this.character = character;
    }

    public Class<?> getType() {
        switch (character) {
            case 'b':
                return boolean.class;
            case 'i':
                return int.class;
            case 'd':
                return double.class;
            case 'c':
                return char.class;
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
}
