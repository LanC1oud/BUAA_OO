import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;
import jdk.jfr.internal.tool.Main;

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
        PersonRequest request = requests.getRequest();
        if (request == null) {
            return false;
        }
        Integer bestElevator = getBestElevator(request);
        if (bestElevator == -1) {
        
        }
        return true;
    }
    
    private Integer getBestElevator(Request request) {
        int bestElevator = -1;
        if (request instanceof MaintRequest) {
            bestElevator = ((MaintRequest) request).getElevatorId();
            elevatorTableMap.get(bestElevator).addMaintRequest((MaintRequest) request);
        } else {
            MyPersonRequest myPersonRequest = (MyPersonRequest) request;
            synchronized (elevatorTableMap.get(1)) {
                synchronized (elevatorTableMap.get(2)) {
                    synchronized (elevatorTableMap.get(3)) {
                        synchronized (elevatorTableMap.get(4)) {
                            synchronized (elevatorTableMap.get(5)) {
                                synchronized (elevatorTableMap.get(6)) {
                                    double maxScore = Integer.MIN_VALUE;
                                    int maxNum = 13;
                                    for (int i = 1; i <= 6; i++) {
                                        ElevatorTable elevatorTable = elevatorTableMap.get(i);
                                        if (!elevatorTable.isMaint() && elevatorTable.getCurrentNum() < maxNum) {
                                            double tempScore = calScore(elevatorTable,myPersonRequest);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestElevator;
    }
    
    public double calScore(ElevatorTable elevatorTable, MyPersonRequest request) {
        int curFloor = FloorTranslator.strToInt(request.getCurrentFloor());
        int toFloor = FloorTranslator.strToInt(request.getToFloor());
        int eleFloor = elevatorTable.getCurrentFloor();
        int leftSpace = elevatorTable.getMaxNumber();
        boolean direction = elevatorTable.getDirection();
        int curNum = elevatorTable.getCurrentNum();
        int totalNum = elevatorTable.getTotalNum();
        int curWeight = elevatorTable.getTotalWeight();
        int speed = 400;
        int distance = getDistance();
        double result = 21 - 2 * distance + 2 * leftSpace - 2.5 * curNum
                - 2.5 *
    }
}