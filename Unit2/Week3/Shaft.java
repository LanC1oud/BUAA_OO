public class Shaft {
    private int currentOnF2 = 0;
    private int wantToF2 = 0;

    private ElevatorTable table1;
    private ElevatorTable table2;
    
    public synchronized void setTable1(ElevatorTable t) { this.table1 = t; }
    
    public synchronized void setTable2(ElevatorTable t) { this.table2 = t; }
    
    public synchronized void acquireF2(int type) {
        wantToF2 = type;

        if (currentOnF2 != 0 && currentOnF2 != type) {
            if (currentOnF2 == 1 && table1 != null) {
                synchronized (table1) {
                    table1.notifyAll();
                }
            } else if (currentOnF2 == 2 && table2 != null) {
                synchronized (table2) {
                    table2.notifyAll();
                }
            }
        }
        
        while (currentOnF2 != 0 && currentOnF2 != type) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        currentOnF2 = type;
        wantToF2 = 0;
    }
    
    public synchronized void releaseF2(int type) {
        if (currentOnF2 == type) {
            currentOnF2 = 0;
            notifyAll();
        }
    }
    
    public synchronized boolean isOpponentApproaching(int myType) {
        return wantToF2 != 0 && wantToF2 != myType;
    }
}