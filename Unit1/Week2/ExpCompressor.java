import java.math.BigInteger;
import java.util.Map;

public class ExpCompressor {

    public static String getShortestExpString(Poly inner) {
        if (inner.getTerms().isEmpty()) {
            return "1";
        }
        
        String originalBody = inner.toString();
        String bestStr;
        if (inner.getTerms().size() > 1) {
            bestStr = "exp((" + originalBody + "))";
        } else {
            bestStr = "exp(" + originalBody + ")";
        }

        BigInteger g = getGcd(inner);

        if (g.compareTo(BigInteger.ONE) > 0) {
            Poly simplified = divide(inner, g);
            String simpleBody = simplified.toString();

            String alternative;
            if (simplified.getTerms().size() > 1) {
                alternative = "exp((" + simpleBody + "))^" + g;
            } else {
                alternative = "exp(" + simpleBody + ")^" + g;
            }

            if (alternative.length() < bestStr.length()) {
                bestStr = alternative;
            }
        }
        
        return bestStr;
    }
    
    private static BigInteger getGcd(Poly p) {
        BigInteger res = BigInteger.ZERO;
        for (BigInteger coeff : p.getTerms().values()) {
            res = res.gcd(coeff.abs());
        }
        return res.equals(BigInteger.ZERO) ? BigInteger.ONE : res;
    }
    
    private static Poly divide(Poly p, BigInteger g) {
        Poly res = new Poly();
        for (Map.Entry<TermKey, BigInteger> entry : p.getTerms().entrySet()) {
            res.addTerm(entry.getKey(), entry.getValue().divide(g));
        }
        return res;
    }
}