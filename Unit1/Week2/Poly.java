import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class Poly {
    private final TreeMap<TermKey, BigInteger> terms = new TreeMap<>();
    
    public TreeMap<TermKey, BigInteger> getTerms() {
        return terms;
    }
    
    public Poly() {}
    
    public Poly(int exp, BigInteger coeff) {
        if (!coeff.equals(BigInteger.ZERO)) {
            addTerm(new TermKey(exp, new Poly()), coeff);
        }
    }
    
    public Poly(BigInteger constant) {
        if (!constant.equals(BigInteger.ZERO)) {
            addTerm(new TermKey(0, new Poly()), constant);
        }
    }
    
    public void addTerm(TermKey key, BigInteger coeff) {
        if (coeff.equals(BigInteger.ZERO)) { return; }
        BigInteger current = terms.getOrDefault(key, BigInteger.ZERO);
        BigInteger next = current.add(coeff);
        if (next.equals(BigInteger.ZERO)) {
            terms.remove(key);
        } else {
            terms.put(key, next);
        }
    }
    
    public void addPoly(Poly other) {
        for (Map.Entry<TermKey, BigInteger> entry : other.terms.entrySet()) {
            this.addTerm(entry.getKey(), entry.getValue());
        }
    }
    
    public void subPoly(Poly other) {
        for (Map.Entry<TermKey, BigInteger> entry : other.terms.entrySet()) {
            this.addTerm(entry.getKey(), entry.getValue().negate());
        }
    }
    
    public Poly multiply(Poly other) {
        Poly res = new Poly();
        for (Map.Entry<TermKey, BigInteger> e1 : this.terms.entrySet()) {
            for (Map.Entry<TermKey, BigInteger> e2 : other.terms.entrySet()) {
                int newExpX = e1.getKey().getExpX() + e2.getKey().getExpX();
                Poly newExpPoly = new Poly();
                newExpPoly.addPoly(e1.getKey().getExpPoly());
                newExpPoly.addPoly(e2.getKey().getExpPoly());
                
                res.addTerm(new TermKey(newExpX, newExpPoly),e1.getValue().multiply(e2.getValue()));
            }
        }
        return res;
    }

    public Poly pow(int exponent) {
        if (exponent == 0) { return new Poly(BigInteger.ONE); }
        Poly res = new Poly(BigInteger.ONE);
        Poly base = this;
        int e = exponent;
        while (e > 0) {
            if (e % 2 == 1) { res = res.multiply(base); }
            base = base.multiply(base);
            e /= 2;
        }
        return res;
    }
    
    public boolean isZero() {
        return terms.isEmpty();
    }
    
    public BigInteger getConstantValue() {
        if (terms.size() == 1) {
            TermKey key = terms.firstKey();
            if (key.getExpX() == 0 && key.getExpPoly().isZero()) {
                return terms.get(key);
            }
        }
        return BigInteger.ZERO;
    }
    
    public Poly substitute(Poly target) {
        Poly result = new Poly();
        for (Map.Entry<TermKey, BigInteger> entry : this.terms.entrySet()) {
            TermKey key = entry.getKey();
            BigInteger coeff = entry.getValue();
            
            Poly termPoly = new Poly(coeff);
            
            Poly subbedX = target.pow(key.getExpX());
            
            Poly innerSubbed = key.getExpPoly().substitute(target);
            Poly expSubbed = new Poly();
            
            expSubbed.addTerm(new TermKey(0, innerSubbed), BigInteger.ONE);
            
            Poly finalTerm = termPoly.multiply(subbedX).multiply(expSubbed);
            
            result.addPoly(finalTerm);
        }
        if (this.terms.isEmpty()) { return new Poly(); }
        return result;
    }
    
    @Override
    public String toString() {
        return OutputHandler.outputAns(this);
    }
}