import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.MaintRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private Requests requests;
    
    public InputThread(Requests requests) {
        this.requests = requests;
    }
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                requests.setEndFlag();
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    MyPersonRequest myPersonRequest = new MyPersonRequest(
                            personRequest.getFromFloor(),personRequest.getToFloor(),
                            personRequest.getPersonId(),personRequest.getWeight());
                    requests.addRequest(myPersonRequest);
                    requests.countPlus();
                } else if (request instanceof MaintRequest) {
                    MaintRequest maintRequest = (MaintRequest) request;
                    requests.addRequest(maintRequest);
                    requests.countPlus();
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}