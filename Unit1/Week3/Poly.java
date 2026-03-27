import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class Poly {
    private final TreeMap<TermKey, BigInteger> terms = new TreeMap<>();
    
    public TreeMap<TermKey, BigInteger> getTerms() {
        return terms;
    }
    
    public Poly() {}
    
    public Poly(BigInteger expX, BigInteger expY, BigInteger coeff) {
        if (!coeff.equals(BigInteger.ZERO)) {
            addTerm(new TermKey(expX, expY, new Poly()), coeff);
        }
    }
    
    public Poly(BigInteger constant) {
        if (!constant.equals(BigInteger.ZERO)) {
            addTerm(new TermKey(BigInteger.ZERO, BigInteger.ZERO, new Poly()), constant);
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
                BigInteger newExpX = e1.getKey().getExpX().add(e2.getKey().getExpX());
                BigInteger newExpY = e1.getKey().getExpY().add(e2.getKey().getExpY());
                Poly newExpPoly = new Poly();
                newExpPoly.addPoly(e1.getKey().getExpPoly());
                newExpPoly.addPoly(e2.getKey().getExpPoly());
                
                res.addTerm(new TermKey(newExpX, newExpY, newExpPoly),
                        e1.getValue().multiply(e2.getValue()));
            }
        }
        return res;
    }

    public Poly pow(BigInteger exponent) {
        if (exponent.equals(BigInteger.ZERO)) { return new Poly(BigInteger.ONE); }
        Poly res = new Poly(BigInteger.ONE);
        Poly base = this;
        BigInteger e = exponent;
        while (e.compareTo(BigInteger.ZERO) > 0) {
            if (e.testBit(0)) { res = res.multiply(base); }
            base = base.multiply(base);
            e = e.shiftRight(1);
        }
        return res;
    }
    
    public boolean isZero() {
        return terms.isEmpty();
    }
    
    public BigInteger getConstantValue() {
        if (terms.size() == 1) {
            TermKey key = terms.firstKey();
            if (key.getExpX().equals(BigInteger.ZERO) &&
                    key.getExpY().equals(BigInteger.ZERO) &&
                    key.getExpPoly().isZero()) {
                return terms.get(key);
            }
        }
        return BigInteger.ZERO;
    }
    
    public Poly derivative(String var) {
        Poly res = new Poly();
        
        for (Map.Entry<TermKey, BigInteger> entry : terms.entrySet()) {
            TermKey key = entry.getKey();
            BigInteger coeff = entry.getValue();
            
            BigInteger expX = key.getExpX();
            BigInteger expY = key.getExpY();
            Poly expPoly = key.getExpPoly();
            
            if (var.equals("x") && expX.compareTo(BigInteger.ZERO) > 0) {
                BigInteger newCoeff = coeff.multiply(expX); // 系数乘下来
                BigInteger newExpX = expX.subtract(BigInteger.ONE); // 指数减一
                res.addTerm(new TermKey(newExpX, expY, expPoly), newCoeff);
            }
            else if (var.equals("y") && expY.compareTo(BigInteger.ZERO) > 0) {
                BigInteger newCoeff = coeff.multiply(expY);
                BigInteger newExpY = expY.subtract(BigInteger.ONE);
                res.addTerm(new TermKey(expX, newExpY, expPoly), newCoeff);
            }

            if (!expPoly.isZero()) {
                Poly selfTerm = new Poly();
                selfTerm.addTerm(key, coeff);
                Poly derivExp = expPoly.derivative(var);
                res.addPoly(selfTerm.multiply(derivExp));
            }
        }
        
        return res;
    }
    
    @Override
    public String toString() {
        return OutputHandler.outputAns(this);
    }
}