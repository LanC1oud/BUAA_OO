import com.oocourse.elevator3.Request;

import java.util.ArrayList;

public class Requests {
    private boolean endFlag;
    private final ArrayList<Request> requestList;
    private int requestCount = 0;
    
    public Requests() {
        endFlag = false;
        requestList = new ArrayList<>();
    }
    
    public synchronized void addRequest(Request request) {
        requestList.add(request);
        this.notifyAll();
    }
    
    public synchronized Request getRequest() {
        while (requestList.isEmpty() && !this.endFlag) {
            try { this.wait(); }
            catch (InterruptedException e) { return null; }
        }
        if (!requestList.isEmpty()) { return requestList.remove(0); }
        return null;
    }
    
    public synchronized void setEndFlag() {
        this.notifyAll();
        endFlag = true;
    }
    
    public synchronized boolean isOver() {
        return this.endFlag && this.requestCount == 0;
    }
    
    public synchronized void countPlus() { requestCount++; }
    
    public synchronized void countMinus() {
        requestCount--;
        if (requestCount == 0) {
            this.notifyAll();
        }
    }
}