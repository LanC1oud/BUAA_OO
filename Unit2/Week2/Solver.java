import java.util.ArrayList;

public class Solver {
    private final ElevatorTable table;
    
    public Solver(ElevatorTable table) { this.table = table; }
    
    public Instr getInstr() {
        if (!table.getMaintList().isEmpty() && table.getMaintFlag()) {
            return Instr.MAINT;
        }
        
        int cur = table.getCurrentFloor();
        if (canOpen(cur)) {
            return Instr.OPEN;
        }
        
        if (table.getCurrentNum() != 0) {
            if (hasInsideTargetInDir(cur, table.getDirection())) {
                return Instr.MOVE;
            }
            return Instr.REVERSE;
        }
        
        if (table.getWaitingList().isEmpty()) {
            return table.isEnd() ? Instr.OVER : Instr.WAIT;
        }
        
        if (hasReqInDir(cur, table.getDirection())) {
            return Instr.MOVE;
        }
        return Instr.REVERSE;
    }
    
    private boolean canOpen(int cur) {
        ArrayList<MyPersonRequest> toHere = table.getToList().get(cur);
        if (toHere != null && !toHere.isEmpty()) {
            return true;
        }
        ArrayList<MyPersonRequest> fromHere = table.getFromList().get(cur);
        if (fromHere != null && !fromHere.isEmpty() &&
                table.getCurrentNum() < table.getMaxNumber() &&
                table.getTotalWeight() < 400) {
            if (table.getCurrentNum() == 0) {
                return true;
            }
            for (MyPersonRequest p : fromHere) {
                if (((FlTr.strToInt(p.getToFloor()) > cur) == table.getDirection()) &&
                        (table.getTotalWeight() + p.getWeight() <= 400)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasInsideTargetInDir(int cur, boolean dir) {
        for (Integer f : table.getToList().keySet()) {
            if ((dir && f > cur || !dir && f < cur) && !table.getToList().get(f).isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasReqInDir(int cur, boolean dir) {
        for (Integer f : table.getFromList().keySet()) {
            if ((dir && f > cur || !dir && f < cur) && !table.getFromList().get(f).isEmpty()) {
                return true;
            }
        }
        return hasInsideTargetInDir(cur, dir);
    }
}