package Catalog;
public interface SchemaAttribute {
    public Class<?> getType();

    public int getLength();

    public boolean isNotNull();

    public String getTypeAsString();

    public boolean isPrimaryKey();

    public String getAttributeName();

    public boolean isUnique();

    public Object getDefault();
}
