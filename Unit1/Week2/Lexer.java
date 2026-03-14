public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;
    
    public Lexer(String input) {
        this.input = input;
        this.next();
    }
    
    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }
    
    private String getIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos += 1;
        }
        return sb.toString();
    }
    
    public void next() {
        if (pos == input.length()) {
            curToken = "";
            return;
        }
        
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if ("[]?:()+*-^".indexOf(c) != -1) {
            pos += 1;
            curToken = String.valueOf(c);
        } else if (Character.isLetter(c)) {
            curToken = getIdentifier();
        } else if (c == '=') {
            pos += 2;
            curToken = "==";
        }
    }
    
    public void nextTwo() {
        this.next();
        this.next();
    }
    
    public String peek() {
        return this.curToken;
    }
}
