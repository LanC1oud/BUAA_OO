import com.oocourse.library1.LibraryBookIsbn;

public class Appointment {
    private final String studentId;
    private final LibraryBookIsbn isbn;
    
    public Appointment(String studentId, LibraryBookIsbn isbn) {
        this.studentId = studentId;
        this.isbn = isbn;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public LibraryBookIsbn getBookIsbn() {
        return isbn;
    }
}