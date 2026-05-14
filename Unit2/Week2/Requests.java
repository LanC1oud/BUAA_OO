import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.Request;

import java.util.ArrayList;

public class Requests {
    private boolean endFlag;
    private ArrayList<MyPersonRequest> myPersonRequestList;
    private ArrayList<MaintRequest> maintRequestList;
    private boolean returnNullFlag = false;
    private int requestCount = 0;
    
    public Requests() {
        endFlag = false;
        myPersonRequestList = new ArrayList<>();
        maintRequestList = new ArrayList<>();
    }
    
    public synchronized void addRequest(Request request) {
        if (request instanceof MyPersonRequest) {
            myPersonRequestList.add((MyPersonRequest) request);
        } else if (request instanceof MaintRequest) {
            maintRequestList.add((MaintRequest) request);
        }
        this.notifyAll();
    }
    
    public synchronized Request getRequest() {
        while (myPersonRequestList.isEmpty() && maintRequestList.isEmpty() && !this.endFlag) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        if (!maintRequestList.isEmpty()) { return maintRequestList.remove(0); }
        if (!myPersonRequestList.isEmpty()) { return myPersonRequestList.remove(0); }
        return null;
    }
    
    public synchronized void setEndFlag() {
        this.notifyAll();
        endFlag = true;
    }
    
    public synchronized boolean isOver() {
        return this.endFlag && this.requestCount == 0;
    }
    
    public synchronized void returnNull(boolean bool) {
        returnNullFlag = bool;
    }
    
    public int getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
    
    public synchronized void countPlus() {
        requestCount++;
    }
    
    public synchronized void countMinus() {
        requestCount--;
        if (requestCount == 0) { this.notifyAll(); }
    }
    
    public synchronized boolean isAllFinished() {
        return endFlag && requestCount == 0;
    }
}