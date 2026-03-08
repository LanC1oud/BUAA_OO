import java.math.BigInteger;
import java.util.TreeMap;

public class Poly {
    private final TreeMap<Integer, Mono> terms;

    public Poly() {
        this.terms = new TreeMap<>();
    }

    public Poly(BigInteger constant) {
        this();
        if (!constant.equals(BigInteger.ZERO)) {
            addMono(new Mono(constant, 0));
        }
    }

    public Poly(int exp, BigInteger coeff) {
        this();
        if (!coeff.equals(BigInteger.ZERO)) {
            addMono(new Mono(coeff, exp));
        }
    }
    
    public void addMono(Mono mono) {
        int exp = mono.getExp();
        if (terms.containsKey(exp)) {
            BigInteger newCoeff = terms.get(exp).getCoeff().add(mono.getCoeff());
            if (newCoeff.equals(BigInteger.ZERO)) {
                terms.remove(exp);
            } else {
                terms.put(exp, new Mono(newCoeff, exp));
            }
        } else {
            terms.put(exp, mono);
        }
    }
    
    public void addPoly(Poly other) {
        for (Mono m : other.terms.values()) {
            this.addMono(m);
        }
    }
    
    public void subPoly(Poly other) {
        for (Mono m : other.terms.values()) {
            this.addMono(m.negate());
        }
    }
    
    public Poly multiply(Poly other) {
        Poly res = new Poly();
        for (Mono m1 : this.terms.values()) {
            for (Mono m2 : other.terms.values()) {
                res.addMono(m1.multiply(m2));
            }
        }
        return res;
    }
    
    public Poly pow(int exponent) {
        if (exponent == 0) {
            return new Poly(BigInteger.ONE);
        }
        Poly res = new Poly(BigInteger.ONE);
        for (int i = 0; i < exponent; i++) {
            res = res.multiply(this);
        }
        return res;
    }
    
    public BigInteger getConstantValue() {
        if (terms.isEmpty()) {
            return BigInteger.ZERO;
        }
        return terms.get(0).getCoeff();
    }
    
    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        
        Integer firstKey = null;
        for (int exp: terms.descendingKeySet()) {
            if (terms.get(exp).getCoeff().signum() > 0) {
                firstKey = exp;
                break;
            }
        }
        
        if (firstKey == null) {
            firstKey = terms.lastKey();
        }
        
        Mono firstMono = terms.get(firstKey);
        if (firstMono.getCoeff().signum() < 0) {
            sb.append("-");
        }
        sb.append(firstMono.toAbsString());
        
        for (int exp : terms.descendingKeySet()) {
            if (exp == firstKey) {
                continue;
            }
            Mono m = terms.get(exp);
            BigInteger c = m.getCoeff();
            
            if (c.signum() > 0) {
                sb.append("+");
            } else {
                sb.append("-");
            }
            sb.append(m.toAbsString());
        }
        return sb.toString();
    }
}