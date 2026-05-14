import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;

public class Elevator extends Thread {
    private final Integer id;
    private final ElevatorTable table;
    private final Requests requests; // 持有全局请求队列，用于维修时退回人员
    private final Solver solver;
    private final long moveSpeed = 400;
    private final long maintMoveSpeed = 200;
    private final long maintSpeed = 1000;
    private final long openSpeed = 400;
    private MaintStatus maintStatus = MaintStatus.NORMAL;
    private int maintFloor = 999;
    private static final int MAX_WEIGHT = 400;
    
    public Elevator(Integer id, Requests requests, ElevatorTable table) {
        this.id = id;
        this.requests = requests;
        this.table = table;
        this.solver = new Solver(table);
    }
    
    @Override
    public void run() {
        boolean end = false;
        while (!end) {
            Instr instr;
            synchronized (table) {
                instr = solver.getInstr();
            }
            if (instr == Instr.MAINT) {
                handleMaintSequence();
            } else if (instr == Instr.MOVE) {
                long speed = (maintStatus == MaintStatus.REP_ACCEPT) ? maintMoveSpeed : moveSpeed;
                trySleep(speed);
                synchronized (table) {
                    moveOneFloor();
                    TimableOutput.println("ARRIVE-" +
                            FlTr.intToStr(table.getCurrentFloor()) + "-" + id);
                }
            } else if (instr == Instr.OPEN) {
                String curFlName;
                synchronized (table) {
                    curFlName = FlTr.intToStr(table.getCurrentFloor());
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
            } else if (instr == Instr.OVER) {
                end = true;
            }
        }
    }
    
    private void handleMaintSequence() {
        synchronized (table) { table.setMaintFlag(true); }
        maintOut();
        acceptMaint();
        maint();
        maintTest();
    }
    
    public void maintOut() {
        synchronized (table) {
            maintStatus = MaintStatus.REP_ACCEPT;
            String currentFloorName = FlTr.intToStr(table.getCurrentFloor());
            if (!table.getInsideList().isEmpty()) {
                TimableOutput.println("OPEN-" + currentFloorName + "-" + id);
                trySleep(openSpeed);
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
                    table.removeFromLists(r);
                    table.reduceRequestWeight(r.getWeight());
                }
                TimableOutput.println("CLOSE-" + currentFloorName + "-" + id);
            }
        }
    }
    
    public void acceptMaint() {
        while (true) {
            int cur;
            synchronized (table) {
                cur = table.getCurrentFloor();
                if (cur == 1) { break; }
                table.setDirection(cur <= 1);
            }
            trySleep(moveSpeed);
            synchronized (table) {
                moveOneFloor();
                cur = table.getCurrentFloor();
                TimableOutput.println("ARRIVE-" + FlTr.intToStr(cur) + "-" + id);
                ArrayList<MyPersonRequest> outNow = new ArrayList<>();
                for (MyPersonRequest r : table.getInsideList()) {
                    if (r.getToFloor().equals(FlTr.intToStr(cur))) { outNow.add(r); }
                }
                if (!outNow.isEmpty()) {
                    TimableOutput.println("OPEN-" + FlTr.intToStr(cur) + "-" + id);
                    trySleep(openSpeed);
                    for (MyPersonRequest r : outNow) {
                        TimableOutput.println("OUT-S-" + r.getPersonId() +
                                "-" + FlTr.intToStr(cur) + "-" + id);
                        requests.countMinus();
                        table.removeFromLists(r);
                        table.reduceRequestWeight(r.getWeight());
                    }
                    TimableOutput.println("CLOSE-" + FlTr.intToStr(cur) + "-" + id);
                }
            }
        }
        
        synchronized (table) {
            TimableOutput.println("OPEN-F1-" + id);
            if (!table.getInsideList().isEmpty()) {
                trySleep(openSpeed);
                for (MyPersonRequest r : new ArrayList<>(table.getInsideList())) {
                    if (r.getToFloor().equals("F1")) {
                        TimableOutput.println("OUT-S-" + r.getPersonId() + "-F1-" + id);
                        requests.countMinus();
                    } else {
                        TimableOutput.println("OUT-F-" + r.getPersonId() + "-F1-" + id);
                        r.setCurrentFloor("F1");
                        requests.addRequest(r);
                    }
                    table.removeFromLists(r);
                    table.reduceRequestWeight(r.getWeight());
                }
            }
            trySleep(openSpeed);
            for (MaintRequest mr : new ArrayList<>(table.getMaintList())) {
                maintFloor = FlTr.strToInt(mr.getToFloor());
                table.getInWorkerList().add(mr);
                table.getMaintList().remove(mr);
                TimableOutput.println("IN-" + mr.getWorkerId() + "-F1-" + id);
            }
            TimableOutput.println("CLOSE-F1-" + id);
            TimableOutput.println("MAINT1-BEGIN-" + id);
            maintStatus = MaintStatus.REPAIR;
        }
    }
    
    public void maint() {
        for (MyPersonRequest r : new ArrayList<>(table.getWaitingList())) {
            table.removeFromLists(r);
            requests.addRequest(r);
        }
        trySleep(maintSpeed);
        synchronized (table) {
            TimableOutput.println("MAINT2-BEGIN-" + id);
            maintStatus = MaintStatus.TEST;
        }
    }
    
    public void maintTest() {
        moveDirect(maintFloor);
        moveDirect(1);
        synchronized (table) {
            TimableOutput.println("OPEN-F1-" + id);
            trySleep(openSpeed);
            for (MaintRequest mr : new ArrayList<>(table.getInWorkerList())) {
                TimableOutput.println("OUT-S-" + mr.getWorkerId() + "-F1-" + id);
                requests.countMinus();
            }
            
            table.getInWorkerList().clear();
            TimableOutput.println("CLOSE-F1-" + id);
            TimableOutput.println("MAINT-END-" + id);
            maintFloor = 999;
            maintStatus = MaintStatus.NORMAL;
            table.setMaintFlag(false);
            table.notifyAll();
        }
    }
    
    private void moveDirect(int target) {
        if (target == 999) { return; }
        while (true) {
            int cur;
            synchronized (table) {
                cur = table.getCurrentFloor();
                if (cur == target) { break; }
                table.setDirection(target > cur);
            }
            trySleep(maintMoveSpeed);
            synchronized (table) {
                moveOneFloor();
                TimableOutput.println("ARRIVE-" +
                        FlTr.intToStr(table.getCurrentFloor()) + "-" + id);
            }
        }
    }
    
    public void moveOneFloor() {
        boolean direction = table.getDirection();
        int cur = table.getCurrentFloor();
        int next = direction ? cur + 1 : cur - 1;
        if (next == 0) { next = direction ? 1 : -1; }
        table.setCurrentFloor(next);
    }
    
    public void getOut() {
        int cur = table.getCurrentFloor();
        ArrayList<MyPersonRequest> reqs = table.getToList().get(cur);
        if (reqs == null) { return; }
        for (MyPersonRequest r : new ArrayList<>(reqs)) {
            TimableOutput.println("OUT-S-" + r.getPersonId() + "-" + FlTr.intToStr(cur) + "-" + id);
            requests.countMinus();
            table.removeFromLists(r);
            table.reduceRequestWeight(r.getWeight());
        }
    }
    
    public void getIn() {
        int cur = table.getCurrentFloor();
        ArrayList<MyPersonRequest> fromHere = table.getFromList().get(cur);
        if (fromHere == null || fromHere.isEmpty()) { return; }
        while (table.getInsideList().size() < table.getMaxNumber()
                && table.getTotalWeight() < MAX_WEIGHT) {
            MyPersonRequest best = null;
            for (MyPersonRequest p : fromHere) {
                boolean personDir = FlTr.strToInt(p.getToFloor()) > cur;
                if (table.getInsideList().isEmpty()) {
                    table.setDirection(personDir);
                    best = p;
                    break;
                }
                else if (personDir == table.getDirection() &&
                        (table.getTotalWeight() + p.getWeight() <= MAX_WEIGHT)) {
                    best = p;
                    break;
                }
            }
            if (best == null) { break; }
            table.getInsideList().add(best);
            table.getToList().get(FlTr.strToInt(best.getToFloor())).add(best);
            fromHere.remove(best);
            table.getWaitingList().remove(best);
            TimableOutput.println("IN-" + best.getPersonId() + "-" + FlTr.intToStr(cur) + "-" + id);
        }
    }
    
    private void trySleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}