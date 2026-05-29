import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;

import static com.oocourse.library1.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class Library {
    private final BookShelf bookshelf;
    private final AppointmentOffice appointmentOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final UserTable userTable;
    
    public Library(Map<LibraryBookIsbn, Integer> bookMap) {
        bookshelf = new BookShelf();
        appointmentOffice = new AppointmentOffice();
        borrowAndReturnOffice = new BorrowAndReturnOffice();
        userTable = new UserTable();
        bookshelf.initBookShelf(bookMap);
    }
    
    public void userBorrowBook(LibraryBookIsbn isbn, String studentId, LibraryCommand cmd) {
        User user = userTable.getUser(studentId);
        if (!bookshelf.hasSpareBook(isbn) || !user.canBorrow(isbn)) {
            PRINTER.reject(cmd);
        } else {
            Book book = bookshelf.removeBook(isbn);
            user.addBook(book);
            book.updateState(cmd.getDate(), LibraryBookState.USER);
            PRINTER.accept(cmd, book);
        }
    }
    
    public void userQueryBook(LibraryBookId bookId, LocalDate today) {
        PRINTER.info(today, bookId, bookshelf.getBook(bookId).getMovingTrace());
    }
    
    public void userReturnBook(LibraryBookId bookId, String studentId, LibraryCommand cmd) {
        User user = userTable.getUser(studentId);
        Book book = user.removeBook(bookId);
        borrowAndReturnOffice.addBook(book);
        book.updateState(cmd.getDate(), LibraryBookState.BORROW_RETURN_OFFICE);
        PRINTER.accept(cmd);
    }
    
    public void userOrderBook(LibraryBookIsbn isbn, String studentId, LibraryCommand cmd) {
        User user = userTable.getUser(studentId);
        if (!user.canBorrow(isbn) || user.hasOrder()) {
            PRINTER.reject(cmd);
        } else {
            bookshelf.addAppointment(new Appointment(studentId, isbn));
            user.setHasOrder(true);
            PRINTER.accept(cmd);
        }
    }
    
    public void userPickBook(LibraryBookIsbn isbn, String studentId, LibraryCommand cmd) {
        User user = userTable.getUser(studentId);
        if (!appointmentOffice.hasOrderBook(studentId) || !user.canBorrow(isbn)) {
            PRINTER.reject(cmd);
        } else {
            Book book = appointmentOffice.removeOrderBook(studentId);
            user.addBook(book);
            book.updateState(cmd.getDate(), LibraryBookState.USER);
            user.setHasOrder(false);
            PRINTER.accept(cmd, book);
        }
    }
    
    public void open(LocalDate today) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        
        handleReturnedBooks(today, moveInfos);
        handleOutdatedAppointments(today, moveInfos);
        handleNewAppointments(today, moveInfos);
        
        PRINTER.move(today, moveInfos);
    }
    
    private void handleReturnedBooks(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        HashSet<Book> returnedBooks = borrowAndReturnOffice.removeAllBooks();
        bookshelf.addBooks(returnedBooks);
        for (Book book : returnedBooks) {
            book.updateState(today, LibraryBookState.BOOKSHELF);
            moveInfos.add(new LibraryMoveInfo(book, "bro", "bs"));
        }
    }
    
    private void handleOutdatedAppointments(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        HashSet<Book> outdatedBooks = appointmentOffice.removeAllOutdatedBooks(today);
        bookshelf.addBooks(outdatedBooks);
        for (Book book : outdatedBooks) {
            User user = userTable.getUser(book.getAppointedUserId());
            user.setHasOrder(false);
            book.updateState(today, LibraryBookState.BOOKSHELF);
            moveInfos.add(new LibraryMoveInfo(book, "ao", "bs"));
        }
    }
    
    private void handleNewAppointments(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        HashSet<Book> orderBooks = bookshelf.removeOrderBooks();
        appointmentOffice.addBooks(orderBooks, today);
        for (Book book : orderBooks) {
            book.updateState(today, LibraryBookState.APPOINTMENT_OFFICE);
            moveInfos.add(new LibraryMoveInfo(book, "bs", "ao", book.getAppointedUserId()));
        }
    }
    
    public void close(LocalDate today) {
        PRINTER.move(today);
    }
}