import com.oocourse.elevator2.PersonRequest;

public class MyPersonRequest extends PersonRequest {
    private String currentFloor;
    
    public MyPersonRequest(String fromFloor, String toFloor, int personId, int weight) {
        super(fromFloor, toFloor, personId, weight);
        this.currentFloor = fromFloor;
    }
    
    public String getCurrentFloor() {
        return currentFloor;
    }
    
    public void setCurrentFloor(String currentFloor) {
        this.currentFloor = currentFloor;
    }
    
    public boolean getDirection() {
        return FloorTranslator.strToInt(getToFloor()) >=
                FloorTranslator.strToInt(getCurrentFloor());
    }
}