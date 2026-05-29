import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Book extends LibraryBookId {
    private LibraryBookState state;
    private final ArrayList<LibraryTrace> movingTrace;
    private String appointedUserId;
    private LocalDate startDate;
    
    public Book(LibraryBookIsbn isbn, String copyId) {
        super(isbn.getType(), isbn.getUid(), copyId);
        this.state = LibraryBookState.BOOKSHELF;
        this.movingTrace = new ArrayList<>();
        this.appointedUserId = null;
        this.startDate = null;
    }
    
    public void updateState(LocalDate date, LibraryBookState nextState) {
        movingTrace.add(new LibraryTrace(date, state, nextState));
        state = nextState;
    }
    
    public ArrayList<LibraryTrace> getMovingTrace() {
        return movingTrace;
    }
    
    public String getAppointedUserId() {
        return appointedUserId;
    }
    
    public void setAppointedUserId(String appointedUserId) {
        this.appointedUserId = appointedUserId;
    }
    
    public void setStartDate(LocalDate date) {
        this.startDate = date;
    }
    
    public boolean isOutdated(LocalDate today) {
        return Math.abs(ChronoUnit.DAYS.between(startDate, today)) >= 5;
    }
}