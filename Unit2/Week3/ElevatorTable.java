import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.RecycleRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class ElevatorTable {
    private final HashMap<Integer, ArrayList<MyPersonRequest>> fromList = new HashMap<>();
    private final HashMap<Integer, ArrayList<MyPersonRequest>> toList = new HashMap<>();
    private final ArrayList<MyPersonRequest> waitingList = new ArrayList<>();
    private final ArrayList<MyPersonRequest> insideList = new ArrayList<>();
    private final ArrayList<MaintRequest> maintList = new ArrayList<>();
    private final ArrayList<UpdateRequest> updateList = new ArrayList<>();
    private final ArrayList<RecycleRequest> recycleList = new ArrayList<>();
    
    private int requestWeight = 0;
    private int currentFloor = 1;
    private boolean direction;
    private final int elevatorId;
    private final int maxNumber;
    
    private boolean endFlag = false;
    private boolean waitFlag = false;
    private boolean maintFlag = false;
    private boolean updateFlag = false;
    private boolean recycleFlag = false;
    
    private int activeReceives = 0;
    private int type = 0; // 0=正常, 1=主轿厢, 2=备用轿厢
    private int lowerBound = -4;
    private int upperBound = 7;
    
    private Shaft shaft;
    private ElevatorTable primaryTable;
    private boolean evictFlag = false;
    
    public ElevatorTable(int id, int max, boolean dir) {
        this.elevatorId = id;
        this.maxNumber = max;
        this.direction = dir;
        for (int i = -10; i <= 20; i++) {
            if (i == 0) {
                continue;
            }
            fromList.put(i, new ArrayList<>());
            toList.put(i, new ArrayList<>());
        }
    }
    
    public synchronized void addRequest(Request r) {
        if (r instanceof MyPersonRequest) {
            MyPersonRequest p = (MyPersonRequest) r;
            TimableOutput.println("RECEIVE-" + p.getPersonId() + "-" + elevatorId);
            activeReceives++;
            waitingList.add(p);
            fromList.get(Tool.strToInt(p.getCurrentFloor())).add(p);
            requestWeight += p.getWeight();
        } else if (r instanceof MaintRequest) {
            this.maintFlag = true;
            maintList.add((MaintRequest) r);
        } else if (r instanceof UpdateRequest) {
            this.updateFlag = true;
            updateList.add((UpdateRequest) r);
        } else if (r instanceof RecycleRequest) {
            this.recycleFlag = true;
            recycleList.add((RecycleRequest) r);
        }
        this.notifyAll();
    }
    
    public synchronized void addActiveReceive() { activeReceives++; }
    
    public synchronized void subActiveReceive() { activeReceives--; }
    
    public synchronized int getActiveReceives() { return activeReceives; }
    
    public synchronized void reduceRequestWeight(int w) { requestWeight -= w; }
    
    public synchronized int getTotalWeight() {
        return insideList.stream().mapToInt(MyPersonRequest::getWeight).sum();
    }
    
    public synchronized int getCurrentFloor() { return currentFloor; }
    
    public synchronized void setCurrentFloor(int f) { currentFloor = f; }
    
    public synchronized boolean getDirection() { return direction; }
    
    public synchronized void setDirection(boolean d) { direction = d; }
    
    public synchronized int getNextFloor() {
        int next = direction ? currentFloor + 1 : currentFloor - 1;
        if (next == 0) {
            return direction ? 1 : -1;
        }
        return next;
    }
    
    public synchronized boolean getMaintFlag() { return maintFlag; }
    
    public synchronized void setMaintFlag(boolean m) { maintFlag = m; }
    
    public synchronized boolean getUpdateFlag() { return updateFlag; }
    
    public synchronized void setUpdateFlag(boolean u) { updateFlag = u; }
    
    public synchronized boolean getRecycleFlag() { return recycleFlag; }
    
    public synchronized void setRecycleFlag(boolean r) { recycleFlag = r; }
    
    public synchronized void setWaitFlag(boolean w) { waitFlag = w; }
    
    public synchronized void setEndFlag() {
        endFlag = true;
        if (waitFlag) {
            this.notifyAll();
        }
    }
    
    public synchronized boolean isEnd() { return endFlag; }
    
    public synchronized int getType() { return type; }
    
    public synchronized void setType(int t) { this.type = t; }
    
    public synchronized int getLowerBound() { return lowerBound; }
    
    public synchronized void setLowerBound(int lb) { this.lowerBound = lb; }
    
    public synchronized int getUpperBound() { return upperBound; }
    
    public synchronized void setUpperBound(int ub) { this.upperBound = ub; }
    
    public synchronized Shaft getShaft() { return shaft; }
    
    public synchronized void setShaft(Shaft s) { this.shaft = s; }
    
    public synchronized ElevatorTable getPrimaryTable() { return primaryTable; }
    
    public synchronized void setPrimaryTable(ElevatorTable pt) { this.primaryTable = pt; }
    
    public synchronized int getCurrentNum() { return insideList.size(); }
    
    public int getMaxNumber() { return maxNumber; }
    
    public int getElevatorId() { return elevatorId; }
    
    public int getRequestWeight() {
        int totalWeight = 0;
        for (MyPersonRequest r : waitingList) {
            totalWeight += r.getWeight();
        }
        for (MyPersonRequest r : insideList) {
            totalWeight += r.getWeight();
        }
        return totalWeight;
    }
    
    public HashMap<Integer, ArrayList<MyPersonRequest>> getFromList() { return fromList; }
    
    public HashMap<Integer, ArrayList<MyPersonRequest>> getToList() { return toList; }
    
    public ArrayList<MyPersonRequest> getWaitingList() { return waitingList; }
    
    public ArrayList<MyPersonRequest> getInsideList() { return insideList; }
    
    public ArrayList<MaintRequest> getMaintList() { return maintList; }
    
    public ArrayList<UpdateRequest> getUpdateList() { return updateList; }
    
    public ArrayList<RecycleRequest> getRecycleList() { return recycleList; }
    
    public boolean isEvictFlag() { return evictFlag; }
    
    public void setEvictFlag(boolean evictFlog) { this.evictFlag = evictFlag; }
    
    public synchronized int getInsideFar() {
        int far = currentFloor;
        for (MyPersonRequest p : insideList) {
            int target = Math.min(Math.max(Tool.strToInt(p.getToFloor()), lowerBound), upperBound);
            if (direction == (target > far)) {
                far = target;
            }
        }
        return far;
    }
    
    public synchronized int getWaitingFar(int cur, boolean d) {
        int far = getInsideFar();
        for (MyPersonRequest p : waitingList) {
            int target = Math.min(Math.max(Tool.strToInt(p.getToFloor()), lowerBound), upperBound);
            if ((d && target > far) || (!d && target < far)) {
                far = target;
            }
        }
        return far;
    }
    
    public synchronized void removeFromLists(MyPersonRequest r) {
        if (r == null) {
            return;
        }
        waitingList.remove(r);
        insideList.remove(r);
        ArrayList<MyPersonRequest> fromListAt = fromList.get(Tool.strToInt(r.getCurrentFloor()));
        if (fromListAt != null) {
            fromListAt.remove(r);
        }
        
        for (ArrayList<MyPersonRequest> list : toList.values()) {
            list.remove(r);
        }
    }
}
