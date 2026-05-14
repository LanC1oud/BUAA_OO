import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private final Requests requests;
    
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
                    PersonRequest pr = (PersonRequest) request;
                    requests.addRequest(new MyPersonRequest(pr.getFromFloor(),
                            pr.getToFloor(), pr.getPersonId(), pr.getWeight()));
                } else {
                    requests.addRequest(request);
                }
                requests.countPlus();
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}