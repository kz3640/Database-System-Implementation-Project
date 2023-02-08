package Schema;
public interface SchemaAttribute {
    public Class<?> getType();

    public int getLength();

    public boolean isNotNull();

    public Character getLetter();

    public boolean isPrimaryKey();

    public String getAttributeName();
}