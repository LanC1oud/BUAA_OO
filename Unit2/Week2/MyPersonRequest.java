import com.oocourse.elevator2.PersonRequest;

public class MyPersonRequest extends PersonRequest {
    private String currentFloor;
    private String newFromFloor;
    
    public MyPersonRequest(String fromFloor, String toFloor, int personId, int weight) {
        super(fromFloor, toFloor, personId, weight);
        this.currentFloor = fromFloor;
        this.newFromFloor = null;
    }
    
    public String getCurrentFloor() {
        return currentFloor;
    }
    
    public void setCurrentFloor(String currentFloor) {
        this.currentFloor = currentFloor;
    }

    @Override
    public String getFromFloor() {
        return (newFromFloor == null) ? currentFloor : newFromFloor;
    }

    public void setNewFromFloor(String newFromFloor) {
        this.newFromFloor = newFromFloor;
    }

    public boolean getDirection() {
        return FlTr.strToInt(getToFloor()) >=
                FlTr.strToInt(getCurrentFloor());
    }
}