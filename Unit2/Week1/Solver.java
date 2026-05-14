import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class Solver {
    private ElevatorTable elevatorTable;
    private static final int MAX_WEIGHT = 400; // 与电梯保持一致
    
    public Solver(ElevatorTable elevatorTable) {
        this.elevatorTable = elevatorTable;
    }
    
    public Instr getInstr() {
        int curFloor = elevatorTable.getCurrentFloor();
        int curNum = elevatorTable.getCurrentNum();
        int curWeight = getCurrentTotalWeight();
        boolean direction = elevatorTable.getDirection();
        int maxNumber = elevatorTable.getMaxNumber();
        if (canOpenForOut(curFloor)) {
            return Instr.OPEN;
        }
        if (canOpenForIn(curFloor, curNum, curWeight, maxNumber, direction)) {
            return Instr.OPEN;
        }
        if (curNum != 0) {
            return Instr.MOVE;
        }
        ArrayList<PersonRequest> waitingRequests = elevatorTable.getWaitingList();
        ArrayList<PersonRequest> insideRequests = elevatorTable.getInsideList();
        if (waitingRequests.isEmpty()) {
            if (elevatorTable.isEnd() && insideRequests.isEmpty()) {
                return Instr.OVER;
            } else {
                return Instr.WAIT;
            }
        }
        if (hasReqInOriginDirection(curFloor, direction) ||
                hasInsideReqInDir(curFloor, direction)) {
            return Instr.MOVE;
        } else {
            return Instr.REVERSE;
        }
    }

    public boolean canOpenForOut(int curFloor) {
        return !elevatorTable.getToList().get(curFloor).isEmpty();
    }
    
    public boolean canOpenForIn(int curFloor, int curNum, int curWeight,
                                int maxNumber, boolean direction) {
        ArrayList<PersonRequest> fromFloorRequests =
                elevatorTable.getFromList().get(curFloor);
        if (curNum >= maxNumber || fromFloorRequests.isEmpty()) {
            return false;
        }
        
        for (PersonRequest pr : fromFloorRequests) {
            int from = Elevator.strToInt(pr.getFromFloor());
            int to = Elevator.strToInt(pr.getToFloor());
            boolean dir = judgeDirection(from, to);
            
            if (dir == direction) {
                if (curWeight + pr.getWeight() <= MAX_WEIGHT) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean judgeDirection(int fromFloor, int toFloor) {
        return fromFloor <= toFloor;
    }
    
    public boolean hasReqInOriginDirection(int curFloor, boolean direction) {
        if (direction) {
            for (int floor = curFloor + 1; floor <= 7; floor++) {
                if (floor == 0) {
                    continue;
                }
                if (!elevatorTable.getFromList().get(floor).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int floor = curFloor - 1; floor >= -4; floor--) {
                if (floor == 0) {
                    continue;
                }
                if (!elevatorTable.getFromList().get(floor).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasInsideReqInDir(int curFloor, boolean direction) {
        if (direction) {
            for (int floor = curFloor + 1; floor <= 7; floor++) {
                if (!elevatorTable.getToList().get(floor).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int floor = curFloor - 1; floor >= -4; floor--) {
                if (!elevatorTable.getToList().get(floor).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private int getCurrentTotalWeight() {
        int total = 0;
        for (PersonRequest p : elevatorTable.getInsideList()) {
            total += p.getWeight();
        }
        return total;
    }
}