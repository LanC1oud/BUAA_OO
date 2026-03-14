import java.util.HashMap;
import java.util.Scanner;

public class Solver {
    
    public void solve() {
        HashMap<String, Poly> functions = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < n; i++) {
            String functionDef = scanner.nextLine();
            String[] parts = functionDef.split("=");
            String leftSide = parts[0];
            String rightSide = parts[1];
            String funcName = leftSide.substring(0, leftSide.indexOf('(')).trim();
            Lexer functionLexer = new Lexer(Preprocesser.preprocess(rightSide));
            Parser functionParser = new Parser(functionLexer, functions);
            Poly functionBody = functionParser.parseExpr();
            functions.put(funcName, functionBody);
        }
        String input = scanner.nextLine();
        String fixedInput = Preprocesser.preprocess(input);
        
        Lexer lexer = new Lexer(fixedInput);
        Parser parser = new Parser(lexer, functions);
        
        Poly result = parser.parseExpr();
        System.out.println(result.toString());
    }
}
