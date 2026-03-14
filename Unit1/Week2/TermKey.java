import java.util.Objects;

public class TermKey implements Comparable<TermKey> {
    // x^expX*exp(expPoly)
    private final int expX;
    private final Poly expPoly;
    
    public TermKey(int expX, Poly expPoly) {
        this.expX = expX;
        this.expPoly = expPoly;
    }
    
    public int getExpX() {
        return expX;
    }
    
    public Poly getExpPoly() {
        return expPoly;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof TermKey)) { return false; }
        TermKey other = (TermKey) o;
        return expX == other.expX && Objects.equals(expPoly.toString(),other.expPoly.toString());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(expX, expPoly.toString());
    }
    
    @Override
    public int compareTo(TermKey other) {
        if (this.expX != other.expX) {
            return Integer.compare(this.expX, other.expX);
        }
        return this.expPoly.toString().compareTo(other.expPoly.toString());
    }
}
