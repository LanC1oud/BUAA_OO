import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        Requests requests = new Requests();
        HashMap<Integer, ElevatorTable> elevatorMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            ElevatorTable elevatorTable = new ElevatorTable(i, 6, true);
            elevatorMap.put(i, elevatorTable);
            Elevator elevator = new Elevator(i, requests, elevatorTable);
            elevator.start();
        }
        new InputThread(requests).start();
        new Dispatcher(requests, elevatorMap).start();
    }
}