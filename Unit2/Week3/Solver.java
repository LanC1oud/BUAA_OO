import java.util.ArrayList;

public class Solver {
    private final ElevatorTable table;
    
    public Solver(ElevatorTable table) { this.table = table; }
    
    public Instr getInstr() {
        if (!table.getMaintList().isEmpty() && table.getMaintFlag()) {
            return Instr.MAINT;
        }
        if (!table.getUpdateList().isEmpty() && table.getUpdateFlag()) {
            return Instr.UPDATE;
        }
        if (!table.getRecycleList().isEmpty() && table.getRecycleFlag()) {
            return Instr.RECYCLE;
        }
        
        int cur = table.getCurrentFloor();
        if (canOpen(cur)) {
            return Instr.OPEN;
        }
        if (table.getType() != 0 && cur == 2 && table.getCurrentNum() == 0) {
            if (table.getShaft() != null &&
                    table.getShaft().isOpponentApproaching(table.getType())) {
                table.setDirection(table.getType() == 1);
                return Instr.MOVE;
            }
        }
        
        if (table.getCurrentNum() != 0) {
            if (hasInsideTargetInDir(cur, table.getDirection())) {
                return Instr.MOVE;
            }
            return Instr.REVERSE;
        }
        
        if (table.getActiveReceives() == 0 && !table.getMaintFlag()
                && !table.getUpdateFlag() && !table.getRecycleFlag()) {
            return table.isEnd() ? Instr.OVER : Instr.WAIT;
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
        
        if (table.getType() == 1 && cur == table.getLowerBound()) {
            for (MyPersonRequest p : table.getInsideList()) {
                if (Tool.strToInt(p.getToFloor()) < cur) {
                    return true;
                }
            }
        }
        if (table.getType() == 2 && cur == table.getUpperBound()) {
            for (MyPersonRequest p : table.getInsideList()) {
                if (Tool.strToInt(p.getToFloor()) > cur) {
                    return true;
                }
            }
        }
        
        ArrayList<MyPersonRequest> fromHere = table.getFromList().get(cur);
        if (fromHere != null && !fromHere.isEmpty() &&
                table.getCurrentNum() < table.getMaxNumber() &&
                table.getTotalWeight() < 400) {
            if (table.getCurrentNum() == 0) {
                return true;
            }
            for (MyPersonRequest p : fromHere) {
                if (((Tool.strToInt(p.getToFloor()) > cur) == table.getDirection()) &&
                        (table.getTotalWeight() + p.getWeight() <= 400)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasInsideTargetInDir(int cur, boolean dir) {
        if (dir && cur >= table.getUpperBound()) {
            return false;
        }
        if (!dir && cur <= table.getLowerBound()) {
            return false;
        }
        for (MyPersonRequest p : table.getInsideList()) {
            int target = Math.min(Math.max(Tool.strToInt(p.getToFloor())
                    , table.getLowerBound()), table.getUpperBound());
            if (dir && target > cur) {
                return true;
            }
            if (!dir && target < cur) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasReqInDir(int cur, boolean dir) {
        if (dir && cur >= table.getUpperBound()) {
            return false;
        }
        if (!dir && cur <= table.getLowerBound()) {
            return false;
        }
        for (Integer f : table.getFromList().keySet()) {
            if ((dir && f > cur || !dir && f < cur) && !table.getFromList().get(f).isEmpty()) {
                return true;
            }
        }
        return hasInsideTargetInDir(cur, dir);
    }
}