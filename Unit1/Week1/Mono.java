import java.math.BigInteger;

public class Mono {
    private final BigInteger coeff; // 系数
    private final int exp;          // x 的指数
    
    public Mono(BigInteger coeff, int exp) {
        this.coeff = coeff;
        this.exp = exp;
    }

    public Mono multiply(Mono other) {
        return new Mono(this.coeff.multiply(other.coeff), this.exp + other.exp);
    }

    public Mono negate() {
        return new Mono(this.coeff.negate(), this.exp);
    }
    
    public BigInteger getCoeff() { return coeff; }
    
    public int getExp() { return exp; }

    public String toAbsString() {
        if (coeff.equals(BigInteger.ZERO)) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        BigInteger absCoeff = coeff.abs();
        
        if (exp == 0) {
            sb.append(absCoeff);
        } else {
            if (!absCoeff.equals(BigInteger.ONE)) {
                sb.append(absCoeff).append("*");
            }
            sb.append("x");
            if (exp != 1) {
                sb.append("^").append(exp);
            }
        }
        return sb.toString();
    }
}