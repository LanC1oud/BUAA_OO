import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.Request;
import java.util.HashMap;

public class Dispatcher extends Thread {
    private Requests requests;
    private HashMap<Integer, ElevatorTable> elevatorTableMap;
    
    public Dispatcher(Requests requests, HashMap<Integer, ElevatorTable> elevatorTableMap) {
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
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean dispatch() {
        Request request = requests.getRequest();
        if (request == null) {
            return false;
        }
        Integer bestElevator = getBestElevator(request);
        if (bestElevator == -1) {
            requests.returnNull(true);
            boolean isDisped = dispatch();
            requests.addRequest(request);
            requests.returnNull(false);
            return isDisped;
        }
        return true;
    }
    
    private Integer getBestElevator(Request request) {
        if (request instanceof MaintRequest) {
            int eleId = ((MaintRequest) request).getElevatorId();
            ElevatorTable table = elevatorTableMap.get(eleId);
            if (table != null) {
                table.addRequest(request);
            }
            return eleId;
        }
        MyPersonRequest req = (MyPersonRequest) request;
        int best = -1;
        double maxScore = Integer.MIN_VALUE;
        for (int i = 1; i <= 6; i++) {
            ElevatorTable table = elevatorTableMap.get(i);
            if (table.getMaintFlag()) {
                continue;
            }
            synchronized (table) {
                double score = calScore(table, req);
                if (score > maxScore) {
                    maxScore = score;
                    best = i;
                }
            }
        }
        if (best != -1) {
            elevatorTableMap.get(best).addRequest(request);
        }
        return best;
    }
    
    public double calScore(ElevatorTable elevatorTable, MyPersonRequest request) {
        int curFloor = FlTr.strToInt(request.getCurrentFloor());
        int toFloor = FlTr.strToInt(request.getToFloor());
        int eleFloor = elevatorTable.getCurrentFloor();
        int leftSpace = elevatorTable.getMaxNumber() - elevatorTable.getCurrentNum();
        boolean direction = elevatorTable.getDirection();
        int curNum = elevatorTable.getCurrentNum();
        int curWeight = elevatorTable.getTotalWeight();
        double speed = 400;
        int distance = getDistance(curFloor, toFloor, eleFloor, direction, request, elevatorTable);

        return 21 - 2 * distance + 2 * leftSpace - 2.5 * curNum
                 - speed / 100 - 3.5 * (double) curWeight / 100;
    }
    
    public int getDistance(int curFloor, int toFloor, int eleFloor, boolean dir,
                           MyPersonRequest request, ElevatorTable elevatorTable) {
        if (curFloor == eleFloor) {
            return 0;
        }
        if ((curFloor > eleFloor) == dir) {
            return Math.abs(curFloor - eleFloor);
        }
        
        int insideFar = elevatorTable.getInsideFar();
        int waitingFar = elevatorTable.getWaitingFar(eleFloor, dir);
        int endFloor = ((insideFar > waitingFar) == dir) ? insideFar : waitingFar;
        
        if (dir) {
            if (request.getDirection()) {
                return Math.abs(endFloor - eleFloor) + endFloor + curFloor + 6;
            } else {
                return Math.abs(endFloor - eleFloor) + Math.abs(endFloor - curFloor);
            }
        } else {
            if (request.getDirection()) {
                return Math.abs(endFloor - eleFloor) + Math.abs(endFloor - curFloor);
            } else {
                return Math.abs(endFloor - curFloor) + Math.abs(endFloor - 7)
                        + Math.abs(curFloor - 7);
            }
        }
    }
}