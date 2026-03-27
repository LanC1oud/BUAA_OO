import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;

public class Parser {
    private final Lexer lexer;
    private final HashMap<String, String> functions;
    
    public Parser(Lexer lexer, HashMap<String, String> functions) {
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
            res = parseSelectionFactor();
        } else if (Objects.equals(token, "exp")) {
            lexer.nextTwo();
            Poly innerPoly = parseExpr();
            lexer.next();
            res = new Poly();
            res.addTerm(new TermKey(BigInteger.ZERO, BigInteger.ZERO, innerPoly), BigInteger.ONE);
        } else if (Objects.equals(token, "dx") || Objects.equals(token,"dy")) {
            lexer.nextTwo();
            Poly inner = parseExpr();
            lexer.next();
            res = inner.derivative(token.equals("dx") ? "x" : "y");
        } else if (Objects.equals(token, "grad")) {
            lexer.nextTwo();
            Poly inner = parseExpr();
            lexer.next();
            res = inner.derivative("x");
            res.addPoly(inner.derivative("y"));
        } else if (Objects.equals(token, "f")) {
            res = parseFunction();
        } else if (Objects.equals(token, "(")) {
            lexer.next();
            res = parseExpr();
            lexer.next();
        } else if (Objects.equals(token, "x")) {
            lexer.next();
            res = new Poly(BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE);
        } else if (Objects.equals(token, "y")) {
            lexer.next();
            res = new Poly(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
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
            res = res.pow(parseExpInt());
        }
        return res;
    }
    
    @SuppressWarnings("checkstyle:LocalVariableName")
    private Poly parseFunction() {
        lexer.next();
        String funcKey = "f";
        
        if (lexer.peek().equals("{")) {
            lexer.next();
            String k = lexer.peek();
            lexer.nextTwo();
            funcKey = "f{" + k + "}";
        }
        
        lexer.next();
        Poly arg = parseExpr();
        lexer.next();
        
        String argStr = arg.getTerms().isEmpty() ? "(0)" : "(" + arg + ")";
        
        if (funcKey.startsWith("f{") && !funcKey.equals("f{0}") && !funcKey.equals("f{1}")) {
            int k = Integer.parseInt(funcKey.substring(2, funcKey.length() - 1));
            String template = functions.get("f{n}");
            String defStr = template.replace("{n-1}","{" + (k - 1) + "}")
                    .replace("{n-2}","{" + (k - 2) + "}");
            Poly purePoly = new Parser(new Lexer(defStr),functions).parseExpr();
            functions.put(funcKey, purePoly.toString());
        }
        
        String expandedStr = functions.get(funcKey);
        expandedStr = expandedStr.replaceAll("\\bx\\b", Matcher.quoteReplacement(argStr));

        return new Parser(new Lexer(expandedStr), functions).parseExpr();
    }
    
    private Poly parseSelectionFactor() {
        lexer.nextTwo();
        Poly polyA = parseExpr();
        lexer.next();
        Poly polyB = parseExpr();
        lexer.nextTwo();
        polyA.subPoly(polyB);
        boolean isTrue = polyA.isZero();
        Poly res;
        if (isTrue) {
            res = parseExpr();
            lexer.next();
            skipExpr();
        } else {
            skipExpr();
            lexer.next();
            res = parseExpr();
        }
        lexer.next();
        return res;
    }
    
    private BigInteger parseExpInt() {
        String token = lexer.peek();
        if (token.equals("(")) {
            lexer.next();
            BigInteger val = parseExpr().getConstantValue();
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
            return new BigInteger(sb.toString());
        }
    }
    
    private void skipExpr() {
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) { lexer.next(); }
        skipTerm();
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            lexer.next();
            skipTerm();
        }
    }
    
    private void skipTerm() {
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) { lexer.next(); }
        skipFactor();
        while (lexer.peek().equals("*")) {
            lexer.next();
            skipFactor();
        }
    }
    
    private void skipFactor() {
        String token = lexer.peek();
        
        if (Objects.equals(token, "[")) {
            lexer.nextTwo();
            skipFactor();
            lexer.next();
            skipFactor();
            lexer.nextTwo();
            skipFactor();
            lexer.next();
            skipFactor();
            lexer.next();
        } else if (Objects.equals(token, "dx") || Objects.equals(token, "dy")
                || Objects.equals(token, "grad") || Objects.equals(token, "exp")) {
            lexer.nextTwo();
            skipExpr();
            lexer.next();
        } else if (Objects.equals(token, "f")) {
            lexer.next();
            if (lexer.peek().equals("{")) {
                lexer.next();
                lexer.nextTwo();
            }
            lexer.next();
            skipExpr();
            lexer.next();
        } else if (Objects.equals(token, "(")) {
            lexer.next();
            skipExpr();
            lexer.next();
        } else if (Objects.equals(token, "x") || Objects.equals(token, "y")) {
            lexer.next();
        } else {
            if (lexer.peek().equals("+") || lexer.peek().equals("-")) { lexer.next(); }
            lexer.next();
        }
        while (lexer.peek().equals("^")) {
            lexer.next(); // ^
            if (lexer.peek().equals("(")) {
                lexer.next();
                skipExpr();
                lexer.next();
            } else {
                if (lexer.peek().equals("+") || lexer.peek().equals("-")) { lexer.next(); }
                lexer.next();
            }
        }
    }
}