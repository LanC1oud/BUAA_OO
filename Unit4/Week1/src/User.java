import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import java.util.HashMap;

public class User {
    private final String studentId;
    private Book borrowedB; // B类只能借一本，直接用成员变量
    private final HashMap<LibraryBookIsbn, Book> borrowedC; // C类按ISBN存，每个ISBN限一本
    private boolean hasOrder; // 是否有未处理的预约
    
    public User(String studentId) {
        this.studentId = studentId;
        this.borrowedB = null;
        this.borrowedC = new HashMap<>();
        this.hasOrder = false;
    }

    public boolean canBorrow(LibraryBookIsbn isbn) {
        if (isbn.isTypeA()) {
            return false;
        }
        if (isbn.isTypeB()) {
            return borrowedB == null;
        }
        if (isbn.isTypeC()) {
            return !borrowedC.containsKey(isbn);
        }
        return false;
    }
    
    public void addBook(Book book) {
        if (book.isTypeB()) {
            borrowedB = book;
        } else if (book.isTypeC()) {
            borrowedC.put(book.getBookIsbn(), book);
        }
    }
    
    public Book removeBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            Book temp = borrowedB;
            borrowedB = null;
            return temp;
        } else if (bookId.isTypeC()) {
            return borrowedC.remove(bookId.getBookIsbn());
        }
        return null;
    }

    public boolean hasOrder() {
        return hasOrder;
    }
    
    public void setHasOrder(boolean status) {
        this.hasOrder = status;
    }
    
    public String getStudentId() {
        return studentId;
    }
}