import com.oocourse.elevator3.TimableOutput;

import java.util.concurrent.ConcurrentHashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        Requests requests = new Requests();
        ConcurrentHashMap<Integer, ElevatorTable> elevatorMap = new ConcurrentHashMap<>();
        for (int i = 1; i <= 6; i++) {
            ElevatorTable elevatorTable = new ElevatorTable(i, 6, true);
            Shaft shaft = new Shaft();
            shaft.setTable1(elevatorTable);
            elevatorTable.setShaft(shaft);
            elevatorMap.put(i, elevatorTable);
            Elevator elevator = new Elevator(i, requests, elevatorTable, elevatorMap);
            elevator.start();
        }
        new InputThread(requests).start();
        new Dispatcher(requests, elevatorMap).start();
    }
}
