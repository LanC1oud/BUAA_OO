import com.oocourse.elevator3.TimableOutput;
import java.util.concurrent.ConcurrentHashMap;

public class Updater {
    private final int moveSpeed = 400;
    private final int openSpeed = 400;
    private final Elevator elevator;
    private final Requests requests;
    private final ElevatorTable table;
    private final int id;
    private final ConcurrentHashMap<Integer, ElevatorTable> elevatorMap;
    
    public Updater(Elevator elevator, Requests requests,
                      ElevatorTable table, int id, ConcurrentHashMap<Integer, ElevatorTable> map) {
        this.elevator = elevator;
        this.requests = requests;
        this.table = table;
        this.id = id;
        this.elevatorMap = map;
    }
    
    public void handleUpdateSequence() {
        elevator.moveDirect(3, moveSpeed);
        synchronized (table) {
            TimableOutput.println("OPEN-F3-" + id);
            elevator.trySleep(openSpeed);
            elevator.kickOutAll("F3");
            TimableOutput.println("CLOSE-F3-" + id);
            TimableOutput.println("UPDATE-BEGIN-" + id);
        }
        elevator.trySleep(1000);
        synchronized (table) {
            TimableOutput.println("UPDATE-END-" + id);
            table.setType(1);
            table.setLowerBound(2);
            table.setUpperBound(7);
            table.getUpdateList().remove(0);
            table.setUpdateFlag(false);
        }
        
        ElevatorTable secTable = new ElevatorTable(id + 6, table.getMaxNumber(), true);
        secTable.setType(2);
        secTable.setLowerBound(-4);
        secTable.setUpperBound(2);
        secTable.setShaft(table.getShaft());
        secTable.setPrimaryTable(table);
        table.getShaft().setTable2(secTable);
        elevatorMap.put(id + 6, secTable);
        
        new Elevator(id + 6, requests, secTable, elevatorMap).start();
        requests.countMinus();
    }
}
