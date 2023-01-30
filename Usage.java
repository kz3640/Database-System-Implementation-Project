public enum Usage {
    CREATE_TABLE_SYNTAX("""
            create table <name>
            <attr_name1> <attr_type1> primarykey,
            <attr_name2> <attr_type2>,
            ....
            <attr_nameN> <attr_typeN>
            );"""),
    SELECT_SYNTAX("""
            select *
            from <name>;"""),
    INSERT_SYNTAX("insert into <name> values <tuples>;"),
    DISPLAY_SCHEMA_SYNTAX("display schema"),
    DISPLAY_INFO_SYNTAX("display info <name>");

    private final String usage;

    Usage(String usage){
        this.usage = usage;
    }

    public String getUsage(){
        return usage;
    }
}
