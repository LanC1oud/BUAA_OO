import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;
import java.util.HashMap;

public class ElevatorTable {
    private final HashMap<Integer, ArrayList<MyPersonRequest>> fromList = new HashMap<>();
    private final HashMap<Integer, ArrayList<MyPersonRequest>> toList = new HashMap<>();
    private final ArrayList<MyPersonRequest> waitingList = new ArrayList<>();
    private final ArrayList<MaintRequest> maintList = new ArrayList<>();
    private final ArrayList<MaintRequest> inWorkerList = new ArrayList<>();
    private final ArrayList<MyPersonRequest> kickList = new ArrayList<>();
    private final ArrayList<MyPersonRequest> insideList = new ArrayList<>();
    private int requestWeight = 0;
    private int currentFloor = 1;
    private boolean direction = true;
    private final int elevatorId;
    private final int maxNumber;
    private boolean endFlag = false;
    private boolean waitFlag = false;
    private boolean maintFlag = false;
    
    public ElevatorTable(int id, int max, boolean dir) {
        this.elevatorId = id;
        this.maxNumber = max;
        this.direction = dir;
        for (int i = -10; i <= 20; i++) {
            if (i == 0) { continue; }
            fromList.put(i, new ArrayList<>());
            toList.put(i, new ArrayList<>());
        }
    }
    
    public synchronized void addRequest(Request r) {
        if (r instanceof MyPersonRequest) {
            MyPersonRequest p = (MyPersonRequest) r;
            TimableOutput.println("RECEIVE-" + p.getPersonId() + "-" + elevatorId);
            waitingList.add(p);
            fromList.get(FlTr.strToInt(p.getCurrentFloor())).add(p);
            requestWeight += p.getWeight();
        } else {
            this.maintFlag = true;
            maintList.add((MaintRequest) r);
        }
        this.notifyAll();
    }
    
    public synchronized void reduceRequestWeight(int w) { requestWeight -= w; }
    
    public synchronized int getRequestWeight() { return requestWeight; }
    
    public synchronized int getTotalWeight() {
        int w = 0; for (MyPersonRequest p : insideList) {
            w += p.getWeight();
        }
        return w;
    }
    
    public synchronized int getCurrentFloor() { return currentFloor; }
    
    public synchronized void setCurrentFloor(int f) { currentFloor = f; }
    
    public synchronized boolean getDirection() { return direction; }
    
    public synchronized void setDirection(boolean d) { direction = d; }
    
    public synchronized boolean getMaintFlag() { return maintFlag; }
    
    public synchronized void setMaintFlag(boolean m) { maintFlag = m; }
    
    public synchronized void setWaitFlag(boolean w) { waitFlag = w; }
    
    public synchronized void setEndFlag() {
        endFlag = true;
        if (waitFlag) {
            this.notifyAll();
        }
    }
    
    public synchronized boolean isEnd() { return endFlag; }
    
    public synchronized int getCurrentNum() { return insideList.size(); }
    
    public synchronized int getInsideFar() {
        int far = currentFloor;
        for (MyPersonRequest p : insideList) {
            int target = FlTr.strToInt(p.getToFloor());
            if (direction == (target > far)) {
                far = target;
            }
        }
        return far;
    }
    
    public synchronized int getWaitingFar(int cur, boolean d) {
        int far = getInsideFar();
        for (MyPersonRequest p : waitingList) {
            int target = FlTr.strToInt(p.getToFloor());
            if ((d && target > far) || (!d && target < far)) {
                far = target;
            }
        }
        return far;
    }
    
    public HashMap<Integer, ArrayList<MyPersonRequest>> getFromList() { return fromList; }
    
    public HashMap<Integer, ArrayList<MyPersonRequest>> getToList() { return toList; }
    
    public ArrayList<MyPersonRequest> getWaitingList() { return waitingList; }
    
    public ArrayList<MyPersonRequest> getInsideList() { return insideList; }
    
    public ArrayList<MaintRequest> getMaintList() { return maintList; }
    
    public ArrayList<MaintRequest> getInWorkerList() { return inWorkerList; }
    
    public ArrayList<MyPersonRequest> getKickList() { return kickList; }
    
    public int getMaxNumber() { return maxNumber; }
    
    public int getElevatorId() { return elevatorId; }
    
    public synchronized void removeFromLists(MyPersonRequest r) {
        if (r == null) { return; }
        waitingList.remove(r);
        insideList.remove(r);
        ArrayList<MyPersonRequest> fromListAt = fromList.get(FlTr.strToInt(r.getCurrentFloor()));
        if (fromListAt != null) {
            fromListAt.remove(r);
        }
        ArrayList<MyPersonRequest> toListAt = toList.get(FlTr.strToInt(r.getToFloor()));
        if (toListAt != null) {
            toListAt.remove(r);
        }
    }
}