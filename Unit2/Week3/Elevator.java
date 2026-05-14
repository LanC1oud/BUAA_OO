import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Elevator extends Thread {
    private final Integer id;
    private final ElevatorTable table;
    private final Requests requests;
    private final ConcurrentHashMap<Integer, ElevatorTable> elevatorMap;
    private final Solver solver;
    private final long moveSpeed = 400;
    private final long openSpeed = 400;
    private final Maintainer maintainer;
    private final Updater updater;
    private final Recycler recycler;
    
    public Elevator(Integer id, Requests requests, ElevatorTable table,
                    ConcurrentHashMap<Integer, ElevatorTable> map) {
        this.id = id;
        this.requests = requests;
        this.table = table;
        this.elevatorMap = map;
        this.solver = new Solver(table);
        this.maintainer = new Maintainer(this, requests, table, id);
        this.updater = new Updater(this, requests, table, id, map);
        this.recycler = new Recycler(this, requests, table, id, map);
    }
    
    @Override
    public void run() {
        boolean end = false;
        if (table.getType() == 2) {
            moveDirect(1, moveSpeed);
        }
        
        while (!end) {
            if (table.isEnd()) {
                break;
            }
            Instr instr;
            synchronized (table) { instr = solver.getInstr(); }
            
            if (instr == Instr.MAINT) {
                maintainer.handleMaintSequence();
            }
            else if (instr == Instr.UPDATE) {
                updater.handleUpdateSequence();
            }
            else if (instr == Instr.RECYCLE) {
                recycler.handleRecycleSequence();
            }
            else if (instr == Instr.MOVE) {
                int next = table.getNextFloor();
                if (next == 2 && table.getType() != 0) {
                    table.getShaft().acquireF2(table.getType());
                }
                
                trySleep(moveSpeed);
                
                int cur = table.getCurrentFloor();
                
                synchronized (table) {
                    table.setCurrentFloor(next);
                    TimableOutput.println("ARRIVE-" +
                            Tool.intToStr(table.getCurrentFloor()) + "-" + id);
                }
                
                if (cur == 2 && next != 2 && table.getType() != 0) {
                    table.getShaft().releaseF2(table.getType());
                }
                
            } else if (instr == Instr.OPEN) {
                String curFlName;
                synchronized (table) {
                    curFlName = Tool.intToStr(table.getCurrentFloor());
                    TimableOutput.println("OPEN-" + curFlName + "-" + id);
                    getOut();
                }
                trySleep(openSpeed);
                synchronized (table) {
                    getIn();
                    TimableOutput.println("CLOSE-" + curFlName + "-" + id);
                }
            } else if (instr == Instr.REVERSE) {
                synchronized (table) { table.setDirection(!table.getDirection()); }
            } else if (instr == Instr.WAIT) {
                synchronized (table) {
                    try {
                        table.setWaitFlag(true);
                        table.wait();
                        table.setWaitFlag(false);
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            } else if (instr == Instr.OVER) { end = true; }
        }
    }
    
    public void kickOutAll(String currentFloorName) {
        for (MyPersonRequest r : new ArrayList<>(table.getInsideList())) {
            if (r.getToFloor().equals(currentFloorName)) {
                TimableOutput.println("OUT-S-" + r.getPersonId() +
                        "-" + currentFloorName + "-" + id);
                requests.countMinus();
            } else {
                TimableOutput.println("OUT-F-" + r.getPersonId() +
                        "-" + currentFloorName + "-" + id);
                r.setCurrentFloor(currentFloorName);
                requests.addRequest(r);
            }
            table.subActiveReceive();
            table.removeFromLists(r);
            table.reduceRequestWeight(r.getWeight());
        }
        for (MyPersonRequest r : new ArrayList<>(table.getWaitingList())) {
            table.removeFromLists(r);
            requests.addRequest(r);
        }
    }
    
    public void moveDirect(int target, long speed) {
        while (true) {
            int cur;
            synchronized (table) {
                cur = table.getCurrentFloor();
                if (cur == target) {
                    break;
                }
                table.setDirection(target > cur);
            }
            int next = table.getNextFloor();
            if (next == 2 && table.getType() != 0) {
                table.getShaft().acquireF2(table.getType());
            }
            
            trySleep(speed);
            
            synchronized (table) {
                table.setCurrentFloor(next);
                TimableOutput.println("ARRIVE-" +
                        Tool.intToStr(table.getCurrentFloor()) + "-" + id);
            }
            
            if (cur == 2 && next != 2 && table.getType() != 0) {
                table.getShaft().releaseF2(table.getType());
            }
        }
    }
    
    public void getOut() {
        int cur = table.getCurrentFloor();
        
        for (MyPersonRequest r : new ArrayList<>(table.getInsideList())) {
            int targetFloor = Tool.strToInt(r.getToFloor());
            
            if (targetFloor == cur) {
                TimableOutput.println("OUT-S-" + r.getPersonId() +
                        "-" + Tool.intToStr(cur) + "-" + id);
                requests.countMinus();
                table.subActiveReceive();
                table.removeFromLists(r);
                table.reduceRequestWeight(r.getWeight());
            }
            else if ((table.getType() == 1 && cur == table.getLowerBound()) ||
                    (table.getType() == 2 && cur == table.getUpperBound())) {
                TimableOutput.println("OUT-F-" + r.getPersonId() +
                        "-" + Tool.intToStr(cur) + "-" + id);
                table.subActiveReceive();
                table.removeFromLists(r);
                table.reduceRequestWeight(r.getWeight());
                
                r.setCurrentFloor(Tool.intToStr(cur));
                requests.addRequest(r);
            }
        }
    }
    
    public void getIn() {
        int cur = table.getCurrentFloor();
        ArrayList<MyPersonRequest> fromHere = table.getFromList().get(cur);
        if (fromHere == null || fromHere.isEmpty()) {
            return;
        }
        
        for (MyPersonRequest best : new ArrayList<>(fromHere)) {
            if (table.getInsideList().size() >= table.getMaxNumber() ||
                    table.getTotalWeight() + best.getWeight() > 400) {
                continue;
            }
            
            boolean personDir = Tool.strToInt(best.getToFloor()) > cur;
            if (table.getInsideList().isEmpty()) {
                table.setDirection(personDir);
            } else if (personDir != table.getDirection()) {
                continue;
            }
            
            table.getInsideList().add(best);
            int clampedTarget = Math.min(Math.max(Tool.strToInt(best.getToFloor()),
                    table.getLowerBound()), table.getUpperBound());
            table.getToList().get(clampedTarget).add(best);
            fromHere.remove(best);
            table.getWaitingList().remove(best);
            TimableOutput.println("IN-" + best.getPersonId() + "-" + Tool.intToStr(cur) + "-" + id);
        }
    }
    
    public void trySleep(long time) {
        try { Thread.sleep(time); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
