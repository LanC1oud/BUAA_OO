import java.math.BigInteger;
import java.util.Objects;

public class TermKey implements Comparable<TermKey> {
    // x^expX*exp(expPoly)
    private final BigInteger expX;
    private final BigInteger expY;
    private final Poly expPoly;
    
    public TermKey(BigInteger expX, BigInteger expY, Poly expPoly) {
        this.expX = expX;
        this.expY = expY;
        this.expPoly = expPoly;
    }
    
    public BigInteger getExpX() {
        return expX;
    }
    
    public BigInteger getExpY() {
        return expY;
    }
    
    public Poly getExpPoly() {
        return expPoly;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof TermKey)) { return false; }
        TermKey other = (TermKey) o;
        return Objects.equals(expX, other.expX) &&
                Objects.equals(expY, other.expY) &&
                Objects.equals(expPoly.toString(), other.expPoly.toString());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(expX, expY, expPoly.toString());
    }
    
    @Override
    public int compareTo(TermKey other) {
        int cmpX = this.expX.compareTo(other.expX);
        if (cmpX != 0) { return cmpX; }
        
        int cmpY = this.expY.compareTo(other.expY);
        if (cmpY != 0) { return cmpY; }
        
        java.util.TreeMap<TermKey, BigInteger> map1 = this.expPoly.getTerms();
        java.util.TreeMap<TermKey, BigInteger> map2 = other.expPoly.getTerms();
        if (map1.size() != map2.size()) { return Integer.compare(map1.size(), map2.size()); }
        
        java.util.Iterator<java.util.Map.Entry<TermKey, BigInteger>> it1 =
                map1.entrySet().iterator();
        java.util.Iterator<java.util.Map.Entry<TermKey, BigInteger>> it2 =
                map2.entrySet().iterator();
        
        while (it1.hasNext() && it2.hasNext()) {
            java.util.Map.Entry<TermKey, BigInteger> e1 = it1.next();
            java.util.Map.Entry<TermKey, BigInteger> e2 = it2.next();
            int cmpKey = e1.getKey().compareTo(e2.getKey());
            if (cmpKey != 0) { return cmpKey; }
            int cmpVal = e1.getValue().compareTo(e2.getValue());
            if (cmpVal != 0) { return cmpVal; }
        }
        return 0;
    }
}
