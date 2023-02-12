package Record;

public class RecordAttribute {
    private Class<?> type;
    private Object attribute;
    private int charLength;

    public RecordAttribute(Class<?> type, Object attribute, int charLength) {
        this.type = type;
        this.attribute = attribute;
        this.charLength = charLength;
    }

    // for char(x), get x
    public int getCharLength() {
        return charLength;
    }

    public Object getAttribute() {
        return attribute;
    }

    public Class<?> getType() {
        return type;
    }
}
