import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.UpdateRequest;

import java.util.concurrent.ConcurrentHashMap;

public class Dispatcher extends Thread {
    private final Requests requests;
    private final ConcurrentHashMap<Integer, ElevatorTable> elevatorTableMap;
    
    public Dispatcher(Requests requests,
                      ConcurrentHashMap<Integer, ElevatorTable> elevatorTableMap) {
        this.requests = requests;
        this.elevatorTableMap = elevatorTableMap;
    }
    
    @Override
    public void run() {
        while (true) {
            if (!dispatch()) {
                if (requests.isOver()) {
                    for (ElevatorTable elevatorTable : elevatorTableMap.values()) {
                        elevatorTable.setEndFlag();
                    }
                    break;
                }
                try { sleep(50); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }
    
    public boolean dispatch() {
        Request request = requests.getRequest();
        if (request == null) {
            return false;
        }
        if (request instanceof MaintRequest) {
            int eleId = ((MaintRequest) request).getElevatorId();
            if (elevatorTableMap.containsKey(eleId)) {
                elevatorTableMap.get(eleId).addRequest(request);
            }
            return true;
        } else if (request instanceof UpdateRequest) {
            int eleId = ((UpdateRequest) request).getElevatorId();
            if (elevatorTableMap.containsKey(eleId)) {
                elevatorTableMap.get(eleId).addRequest(request);
            }
            return true;
        } else if (request instanceof RecycleRequest) {
            int eleId = ((RecycleRequest) request).getElevatorId();
            if (elevatorTableMap.containsKey(eleId)) {
                elevatorTableMap.get(eleId).addRequest(request);
            }
            return true;
        }
        
        Integer bestElevator = getBestElevator((MyPersonRequest) request);
        if (bestElevator == -1) {
            requests.addRequest(request);
            return false;
        }
        return true;
    }
    
    private Integer getBestElevator(MyPersonRequest req) {
        int best = -1;
        double maxScore = -100000.0;
        for (ElevatorTable table : elevatorTableMap.values()) {
            if (table.getMaintFlag() || table.getUpdateFlag() || table.getRecycleFlag()) {
                continue;
            }
            if (table.getRequestWeight() >= 400) {
                continue;
            }
            synchronized (table) {
                double score = calScore(table, req);
                if (score > maxScore) {
                    maxScore = score;
                    best = table.getElevatorId();
                }
            }
        }
        if (best != -1) {
            elevatorTableMap.get(best).addRequest(req);
        }
        return best;
    }
    
    public double calScore(ElevatorTable elevatorTable, MyPersonRequest request) {
        int curFloor = Tool.strToInt(request.getCurrentFloor());
        if (curFloor > elevatorTable.getUpperBound()
                || curFloor < elevatorTable.getLowerBound()) {
            return -100000.0;
        }
        
        int toFloor = Tool.strToInt(request.getToFloor());
        int effToFloor = Math.min(Math.max(toFloor, elevatorTable.getLowerBound()),
                elevatorTable.getUpperBound());
        
        if (curFloor == effToFloor) {
            return -100000.0;
        }
        
        int eleFloor = elevatorTable.getCurrentFloor();
        int leftSpace = elevatorTable.getMaxNumber() - elevatorTable.getCurrentNum();
        boolean direction = elevatorTable.getDirection();
        int curNum = elevatorTable.getCurrentNum();
        int curWait = elevatorTable.getWaitingList().size();
        int curWeight = elevatorTable.getTotalWeight();
        int distance = getDistance(curFloor, eleFloor,
                direction, elevatorTable);
        double score = 21 - 2 * distance + 2 * leftSpace -
                curNum - 5 * curWait - 4 - 3.5 * curWeight / 100;
        if (elevatorTable.getType() != 0) {
            score -= 200.0;
        }
        
        return score;
    }
    
    public int getDistance(int curF, int eleF,
                           boolean dir, ElevatorTable t) {
        if (curF == eleF) {
            return 0;
        }
        if ((curF > eleF) == dir) {
            return Math.abs(curF - eleF);
        }
        
        int insideFar = t.getInsideFar();
        int waitingFar = t.getWaitingFar(eleF, dir);
        int endF = ((insideFar > waitingFar) == dir) ? insideFar : waitingFar;
        
        if (dir) {
            return Math.abs(endF - eleF) + Math.abs(endF - curF);
        } else {
            return Math.abs(endF - curF) + Math.abs(endF - 7) + Math.abs(curF - 7);
        }
    }
}
