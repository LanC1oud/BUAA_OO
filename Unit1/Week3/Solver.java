import java.util.HashMap;
import java.util.Scanner;

public class Solver {
    
    public void solve() {
        HashMap<String, String> functions = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < n; i++) {
            String functionDef = scanner.nextLine();
            String[] parts = functionDef.split("=",2);
            String leftSide = parts[0];
            String rightSide = parts[1];
            String funcName = leftSide.substring(0, leftSide.indexOf('(')).trim();
            functions.put(funcName, Preprocesser.preprocess(rightSide));
        }
        int m = Integer.parseInt(scanner.nextLine());
        if (m > 0) {
            for (int i = 0; i < 3; i++) {
                String def = scanner.nextLine().replaceAll("\\s+", "");
                String[] parts = def.split("=",2);
                String leftSide = parts[0];
                String rightSide = parts[1];
                if (leftSide.startsWith("f{0}")) { functions.put("f{0}", rightSide); }
                if (leftSide.startsWith("f{1}")) { functions.put("f{1}", rightSide); }
                if (leftSide.startsWith("f{n}")) { functions.put("f{n}", rightSide); }
            }
        }
        
        String input = scanner.nextLine();
        String fixedInput = Preprocesser.preprocess(input);
        
        Lexer lexer = new Lexer(fixedInput);
        Parser parser = new Parser(lexer, functions);
        
        Poly result = parser.parseExpr();
        System.out.println(result.toString());
    }
}
