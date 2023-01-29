public class SyntaxException extends Exception {
    final String token;
    final String usage;
    public SyntaxException(String badToken, String usage) {
        this.token = badToken;
        this.usage = usage;
    }

    @Override
    public String getMessage() {
        return String.format("Syntax error:\nBad token:%s.\nusage:%s", token, usage);
    }
}
