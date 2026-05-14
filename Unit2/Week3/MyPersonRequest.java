import com.oocourse.elevator3.PersonRequest;

public class MyPersonRequest extends PersonRequest {
    private String currentFloor;
    
    public MyPersonRequest(String fromFloor, String toFloor, int personId, int weight) {
        super(fromFloor, toFloor, personId, weight);
        this.currentFloor = fromFloor;
    }
    
    public String getCurrentFloor() { return currentFloor; }
    
    public void setCurrentFloor(String currentFloor) { this.currentFloor = currentFloor; }
}