import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;
    
    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    public Poly parseExpr() {
        Poly res = new Poly();

        int sign = 1;
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        
        Poly firstTerm = parseTerm();
        if (sign == -1) {
            res.subPoly(firstTerm);
        } else {
            res.addPoly(firstTerm);
        }
        
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            String op = lexer.peek();
            lexer.next();
            Poly nextTerm = parseTerm();
            if (op.equals("+")) {
                res.addPoly(nextTerm);
            } else {
                res.subPoly(nextTerm);
            }
        }
        return res;
    }
    
    public Poly parseTerm() {
        int sign = 1;
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        
        Poly res = parseFactor();
        if (sign == -1) {
            res = res.multiply(new Poly(BigInteger.valueOf(-1)));
        }
        
        while (lexer.peek().equals("*")) {
            lexer.next();
            Poly nextFactor = parseFactor();
            res = res.multiply(nextFactor);
        }
        return res;
    }
    
    public Poly parseFactor() {
        String token = lexer.peek();
        
        if (token.equals("(")) {
            lexer.next();
            Poly exprPoly = parseExpr();
            lexer.next();
            
            if (lexer.peek().equals("^")) {
                lexer.next();
                return exprPoly.pow(parseExp());
            }
            return exprPoly;
        } else if (token.equals("x")) {
            lexer.next();
            int exp = 1;
            if (lexer.peek().equals("^")) {
                lexer.next();
                exp = parseExp();
            }
            return new Poly(exp, BigInteger.ONE);
        } else {
            StringBuilder sb = new StringBuilder();
            if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
                sb.append(lexer.peek());
                lexer.next();
            }
            sb.append(lexer.peek());
            lexer.next();
            return new Poly(new BigInteger(sb.toString()));
        }
    }
    
    private int parseExp() {
        String token = lexer.peek();
        
        if (token.equals("(")) {
            lexer.next();
            Poly res = parseExpr();
            lexer.next();
            return res.getConstantValue().intValue();
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
