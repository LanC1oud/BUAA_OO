import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Elevator extends Thread {
    private Integer id;
    private final ElevatorTable elevatorTable;
    private HashMap<Integer, ArrayList<PersonRequest>> fromList;
    private ArrayList<PersonRequest> waitingList;
    private HashMap<Integer, ArrayList<PersonRequest>> toList;
    private ArrayList<PersonRequest> insideList;
    private Solver solver;
    private final long moveSpeed = 400;
    private final long openSpeed = 400;
    private long lastTime;
    private static final int MAX_WEIGHT = 400;
    
    public Elevator(Integer id, long startTime, ElevatorTable elevatorTable) {
        this.id = id;
        this.elevatorTable = elevatorTable;
        solver = new Solver(elevatorTable);
        lastTime = startTime;
        fromList = elevatorTable.getFromList();
        waitingList = elevatorTable.getWaitingList();
        toList = elevatorTable.getToList();
        insideList = elevatorTable.getInsideList();
    }
    
    public Integer getCurrentNum() {
        return insideList.size();
    }
    
    private int getCurrentTotalWeight() {
        int total = 0;
        for (PersonRequest p : insideList) {
            total += p.getWeight();
        }
        return total;
    }
    
    @Override
    public void run() {
        boolean end = false;
        while (!end) {
            Instr instr;
            synchronized (elevatorTable) {
                instr = solver.getInstr();
            }
            if (instr == Instr.MOVE) {
                try {
                    Thread.sleep(moveSpeed);
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
                synchronized (elevatorTable) {
                    moveOneFloor();
                    TimableOutput.println("ARRIVE-" +
                            intToStr(elevatorTable.getCurrentFloor()) + "-" + id);
                    lastTime = System.currentTimeMillis();
                }
            }
            else if (instr == Instr.OPEN) {
                int currentFloor;
                synchronized (elevatorTable) {
                    currentFloor = elevatorTable.getCurrentFloor();
                    TimableOutput.println("OPEN-" + intToStr(currentFloor) + "-" + id);
                    getOut();
                }
                try {
                    Thread.sleep(openSpeed);
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
                synchronized (elevatorTable) {
                    getIn();
                    TimableOutput.println("CLOSE-" + intToStr(currentFloor) + "-" + id);
                    lastTime = System.currentTimeMillis();
                }
            }
            else if (instr == Instr.REVERSE) {
                synchronized (elevatorTable) {
                    elevatorTable.setDirection(!elevatorTable.getDirection());
                }
            }
            else if (instr == Instr.WAIT) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
            else if (instr == Instr.OVER) {
                end = true;
            }
        }
    }

    public void moveOneFloor() {
        boolean direction = elevatorTable.getDirection();
        int afterFloor;
        if (direction) {
            afterFloor = elevatorTable.getCurrentFloor() + 1;
            if (afterFloor == 0) {
                afterFloor = 1;
            }
        } else {
            afterFloor = elevatorTable.getCurrentFloor() - 1;
            if (afterFloor == 0) {
                afterFloor = -1;
            }
        }
        elevatorTable.setCurrentFloor(afterFloor);
    }
    
    public void getOut() {
        int currentFloor = elevatorTable.getCurrentFloor();
        String currentFloorName = intToStr(currentFloor);
        ArrayList<PersonRequest> requests = toList.get(currentFloor);
        for (PersonRequest request : new ArrayList<>(requests)) {
            TimableOutput.println("OUT-S-" + request.getPersonId() + "-" +
                    currentFloorName + "-" + id);
            insideList.remove(request);
        }
        requests.clear();
    }
    
    public void getIn() {
        int currentFloor = elevatorTable.getCurrentFloor();
        boolean direction = elevatorTable.getDirection();
        int maxNumber = elevatorTable.getMaxNumber();
        ArrayList<PersonRequest> fromFloorRequests2 = fromList.get(currentFloor);
        while (true) {
            if (getCurrentNum() >= maxNumber || getCurrentTotalWeight() >= MAX_WEIGHT) {
                break;
            }
            PersonRequest bestPerson = getRequest(fromFloorRequests2, direction);
            if (bestPerson == null) {
                break;
            }
            insideList.add(bestPerson);
            toList.get(strToInt(bestPerson.getToFloor())).add(bestPerson);
            fromFloorRequests2.remove(bestPerson);
            waitingList.remove(bestPerson);
            TimableOutput.println("IN-" + bestPerson.getPersonId() +
                    "-" + intToStr(currentFloor) + "-" + id);
        }
    }
    
    private PersonRequest getRequest(ArrayList<PersonRequest> personRequests, boolean direction) {
        PersonRequest bestPerson = null;
        int minWeight = 99999;
        for (PersonRequest pr : personRequests) {
            int from = strToInt(pr.getFromFloor());
            int to = strToInt(pr.getToFloor());
            boolean dir = (from <= to);
            if (dir == direction) {
                int futureWeight = getCurrentTotalWeight() + pr.getWeight();
                if (futureWeight <= MAX_WEIGHT && pr.getWeight() < minWeight) {
                    minWeight = pr.getWeight();
                    bestPerson = pr;
                }
            }
        }
        return bestPerson;
    }
    
    public static Integer strToInt(String floorName) {
        char prefix = floorName.charAt(0);
        int num = Integer.parseInt(floorName.substring(1));
        if (prefix == 'B') {  return -num; }
        else { return num; }
    }
    
    public static String intToStr(Integer floorNumber) {
        if (floorNumber < 0) { return "B" + (-floorNumber); }
        else { return "F" + floorNumber; }
    }
}