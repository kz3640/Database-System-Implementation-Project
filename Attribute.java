public class Attribute {
    private String name = null;
    private AttrType type = null;
    private int len = -1;

    private boolean key = false;

    public Attribute(String name, AttrType type, int len, boolean key) {
        this.name = name;
        this.type = type;
        this.len = len;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public AttrType getType() {
        return type;
    }

    public int getLen() {
        return len;
    }

    public boolean isKey() {
        return key;
    }
}
