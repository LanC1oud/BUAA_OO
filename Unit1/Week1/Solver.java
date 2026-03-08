import java.util.Scanner;

public class Solver {
    
    public void solve() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String fixedInput = preprocess(input);
        
        Lexer lexer = new Lexer(fixedInput);
        Parser parser = new Parser(lexer);
        
        Poly result = parser.parseExpr();
        System.out.println(result.toString());
    }
    
    public String preprocess(String input) {
        String result;
        result = input.replaceAll("\\s+","");
        boolean changed = true;
        while (changed) {
            final int len = result.length();
            result = result.replace("++","+");
            result = result.replace("+-","-");
            result = result.replace("-+","-");
            result = result.replace("--","+");
            changed = (result.length() != len);
        }
        return result;
    }
}
