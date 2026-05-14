import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class ElevatorTable {
    private HashMap<Integer, ArrayList<PersonRequest>> fromList;
    private ArrayList<PersonRequest> waitingList;
    private HashMap<Integer, ArrayList<PersonRequest>> toList;
    private ArrayList<PersonRequest> insideList;
    private Integer maxNumber;
    private Integer currentFloor = 1;
    private boolean direction; // true for up, false for down
    private int elevatorId;
    private boolean endFlag = false;
    private boolean waitFlag = false;
    
    public ElevatorTable(int elevatorId, int maxNumber, boolean direction) {
        this.fromList = new HashMap<>();
        for (int i = -4; i <= 7; i++) {
            fromList.put(i, new ArrayList<>());
        }
        this.waitingList = new ArrayList<>();
        this.toList = new HashMap<>();
        for (int i = -4; i <= 7; i++) {
            toList.put(i, new ArrayList<>());
        }
        this.insideList = new ArrayList<>();
        this.elevatorId = elevatorId;
        this.maxNumber = maxNumber;
        this.direction = direction;
    }
    
    public synchronized void addRequest(PersonRequest request) {
        if (waitFlag) {
            this.notifyAll();
        }
        waitingList.add(request);
        fromList.get(getFromFloor(request)).add(request);
    }
    
    public synchronized void setEndFlag() {
        if (waitFlag) {
            this.notifyAll();
        }
        endFlag = true;
    }
    
    public synchronized boolean isEnd() {
        if (waitFlag) {
            this.notifyAll();
        }
        return endFlag;
    }
    
    public Integer getFromFloor(PersonRequest request) {
        String fromFloor = request.getFromFloor();
        return Elevator.strToInt(fromFloor);
    }
    
    public HashMap<Integer, ArrayList<PersonRequest>> getFromList() {
        return fromList;
    }
    
    public ArrayList<PersonRequest> getWaitingList() {
        return waitingList;
    }
    
    public HashMap<Integer, ArrayList<PersonRequest>> getToList() {
        return toList;
    }
    
    public ArrayList<PersonRequest> getInsideList() {
        return insideList;
    }
    
    public Integer getCurrentFloor() {
        return currentFloor;
    }
    
    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor = currentFloor;
    }
    
    public boolean getDirection() {
        return direction;
    }
    
    public void setDirection(boolean direction) {
        this.direction = direction;
    }
    
    public Integer getMaxNumber() {
        return maxNumber;
    }
    
    public Integer getCurrentNum() {
        return insideList.size();
    }
    
    public void setWaitFlag(boolean waitFlag) {
        this.waitFlag = waitFlag;
    }
}