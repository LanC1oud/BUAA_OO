import com.oocourse.elevator3.TimableOutput;
import java.util.concurrent.ConcurrentHashMap;

public class Recycler {
    private final int moveSpeed = 400;
    private final int openSpeed = 400;
    private final Elevator elevator;
    private final Requests requests;
    private final ElevatorTable table;
    private final int id;
    private final ConcurrentHashMap<Integer, ElevatorTable> elevatorMap;
    
    public Recycler(Elevator elevator, Requests requests,
                   ElevatorTable table, int id, ConcurrentHashMap<Integer, ElevatorTable> map) {
        this.elevator = elevator;
        this.requests = requests;
        this.table = table;
        this.id = id;
        this.elevatorMap = map;
    }
    
    public void handleRecycleSequence() {
        elevator.moveDirect(1, moveSpeed);
        synchronized (table) {
            TimableOutput.println("OPEN-F1-" + id);
            elevator.trySleep(openSpeed);
            elevator.kickOutAll("F1");
            TimableOutput.println("CLOSE-F1-" + id);
            TimableOutput.println("RECYCLE-BEGIN-" + id);
        }
        elevator.trySleep(1000);
        synchronized (table) {
            TimableOutput.println("RECYCLE-END-" + id);
            table.getRecycleList().remove(0);
            table.setRecycleFlag(false);
            table.setEndFlag();
        }
        
        ElevatorTable prim = table.getPrimaryTable();
        synchronized (prim) {
            prim.setType(0);
            prim.setLowerBound(-4);
            prim.setUpperBound(7);
            prim.notifyAll();
        }
        elevatorMap.remove(id);
        requests.countMinus();
    }
}
