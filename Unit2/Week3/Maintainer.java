import com.oocourse.elevator3.MaintRequest;
import com.oocourse.elevator3.TimableOutput;

public class Maintainer {
    private final int moveSpeed = 400;
    private final int openSpeed = 400;
    private final Elevator elevator;
    private final Requests requests;
    private final ElevatorTable table;
    private final int id;
    
    public Maintainer(Elevator elevator, Requests requests,
                      ElevatorTable table, int id) {
        this.elevator = elevator;
        this.requests = requests;
        this.table = table;
        this.id = id;
    }
    
    public void handleMaintSequence() {
        elevator.moveDirect(1, moveSpeed);
        MaintRequest req;
        synchronized (table) {
            req = table.getMaintList().get(0);
            TimableOutput.println("OPEN-F1-" + id);
            elevator.trySleep(openSpeed);
            elevator.kickOutAll("F1");
            TimableOutput.println("IN-" + req.getWorkerId() + "-F1-" + id);
            TimableOutput.println("CLOSE-F1-" + id);
            TimableOutput.println("MAINT1-BEGIN-" + id);
        }
        elevator.trySleep(1000);
        synchronized (table) {
            TimableOutput.println("MAINT2-BEGIN-" + id);
        }
        int target = Tool.strToInt(req.getToFloor());
        elevator.moveDirect(target, 200);
        elevator.moveDirect(1, 200);
        synchronized (table) {
            TimableOutput.println("OPEN-F1-" + id);
            elevator.trySleep(openSpeed);
            TimableOutput.println("OUT-S-" + req.getWorkerId() + "-F1-" + id);
            TimableOutput.println("CLOSE-F1-" + id);
            TimableOutput.println("MAINT-END-" + id);
            table.getMaintList().remove(0);
            table.setMaintFlag(false);
        }
        requests.countMinus();
    }
}
