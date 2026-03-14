import java.math.BigInteger;
import java.util.TreeMap;

public class OutputHandler {
    public static String outputAns(Poly poly) {
        TreeMap<TermKey, BigInteger> terms = poly.getTerms();
        if (terms.isEmpty()) { return "0"; }
        
        StringBuilder sb = new StringBuilder();
        
        TermKey firstKey = null;
        for (TermKey key : terms.descendingKeySet()) {
            if (terms.get(key).signum() > 0) {
                firstKey = key;
                break;
            }
        }
        if (firstKey == null) { firstKey = terms.lastKey(); }

        appendTerm(sb, firstKey, terms.get(firstKey), true);

        for (TermKey key : terms.descendingKeySet()) {
            if (key == firstKey) {
                continue;
            }
            appendTerm(sb, key, terms.get(key), false);
        }
        
        return sb.toString();
    }
    
    private static void appendTerm(StringBuilder sb, TermKey key,
                                   BigInteger coeff, boolean isFirst) {
        if (coeff.equals(BigInteger.ZERO)) { return; }
        
        if (coeff.signum() > 0) {
            if (!isFirst) { sb.append("+"); }
        }
        else { sb.append("-"); }
        
        BigInteger absCoeff = coeff.abs();
        int expX = key.getExpX();
        Poly expPoly = key.getExpPoly();
        boolean hasExp = !expPoly.isZero();
        
        if (expX == 0 && !hasExp) {
            sb.append(absCoeff);
            return;
        }
        
        boolean printedBefore = false;
        
        if (!absCoeff.equals(BigInteger.ONE)) {
            sb.append(absCoeff);
            printedBefore = true;
        }
        
        if (expX != 0) {
            if (printedBefore) { sb.append("*"); }
            if (expX == 1) { sb.append("x"); }
            else { sb.append("x^").append(expX); }
            printedBefore = true;
        }
        
        if (hasExp) {
            if (printedBefore) { sb.append("*"); }
            sb.append(ExpCompressor.getShortestExpString(expPoly));
        }
    }
}