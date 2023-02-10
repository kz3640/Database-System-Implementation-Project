import java.util.regex.*;


public enum AttrType {
    INTEGER("Integer"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    CHAR("Char\\([1-9][0-9]*\\)"),
    VARCHAR("Varchar\\([1-9][0-9]*\\)");

    private Pattern regex;
    AttrType(String regex) {
        regex = regex;
    }
    public boolean isMatch(String test) {
        return regex.matcher(test).matches();
    }


}
