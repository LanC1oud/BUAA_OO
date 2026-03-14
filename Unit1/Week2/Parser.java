import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;

public class Parser {
    private final Lexer lexer;
    private final HashMap<String, Poly> functions;
    
    public Parser(Lexer lexer, HashMap<String, Poly> functions) {
        this.lexer = lexer;
        this.functions = functions;
    }

    public Poly parseExpr() {
        Poly res = new Poly();
        int sign = 1;
        if (lexer.peek().equals("+")) { lexer.next(); }
        else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        
        Poly firstTerm = parseTerm();
        if (sign == 1) { res.addPoly(firstTerm); }
        else { res.subPoly(firstTerm); }
        
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            String op = lexer.peek();
            lexer.next();
            Poly nextTerm = parseTerm();
            if (op.equals("+")) { res.addPoly(nextTerm); }
            else { res.subPoly(nextTerm); }
        }
        return res;
    }

    public Poly parseTerm() {
        int sign = 1;
        if (lexer.peek().equals("+")) { lexer.next(); }
        else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        
        Poly res = parseFactor();
        if (sign == -1) { res = res.multiply(new Poly(BigInteger.valueOf(-1))); }
        
        while (lexer.peek().equals("*")) {
            lexer.next();
            res = res.multiply(parseFactor());
        }
        return res;
    }

    public Poly parseFactor() {
        Poly res;
        String token = lexer.peek();

        if (Objects.equals(token, "[")) {
            Poly diff = new Poly();
            lexer.nextTwo();
            Poly polyA = parseFactor();
            diff.addPoly(polyA);
            lexer.next();
            Poly polyB = parseFactor();
            diff.subPoly(polyB);
            lexer.nextTwo();
            Poly polyC = parseFactor();
            lexer.next();
            Poly polyD = parseFactor();
            lexer.next();
            if (diff.isZero()) { return polyC; }
            else { return polyD; }
        } else if (Objects.equals(token, "exp")) {
            lexer.nextTwo();
            Poly innerPoly = parseExpr();
            lexer.next();
            res = new Poly();
            res.addTerm(new TermKey(0, innerPoly), BigInteger.ONE);
        } else if (functions != null && functions.containsKey(token)) {
            String funcName = token;
            lexer.next();
            lexer.next();
            Poly arg = parseExpr();
            lexer.next();
            return functions.get(funcName).substitute(arg);
        } else if (Objects.equals(token, "(")) {
            lexer.next();
            res = parseExpr();
            lexer.next();
        } else if (Objects.equals(token, "x")) {
            lexer.next();
            res = new Poly(1, BigInteger.ONE);
        } else {
            StringBuilder sb = new StringBuilder();
            if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
                sb.append(lexer.peek());
                lexer.next();
            }
            sb.append(lexer.peek());
            lexer.next();
            res = new Poly(new BigInteger(sb.toString()));
        }

        while (lexer.peek().equals("^")) {
            lexer.next();
            int exponent = parseExpInt();
            res = res.pow(exponent);
        }
        return res;
    }
    
    private int parseExpInt() {
        String token = lexer.peek();
        if (token.equals("(")) {
            lexer.next();
            int val = parseExpr().getConstantValue().intValue();
            lexer.next();
            return val;
        } else {
            StringBuilder sb = new StringBuilder();
            if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
                sb.append(lexer.peek());
                lexer.next();
            }
            sb.append(lexer.peek());
            lexer.next();
            return Integer.parseInt(sb.toString());
        }
    }
}