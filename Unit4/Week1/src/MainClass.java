import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;
import java.util.Map;

import static com.oocourse.library1.LibraryIO.SCANNER;
import static com.oocourse.library1.LibraryReqCmd.Type.QUERIED;
import static com.oocourse.library1.LibraryReqCmd.Type.RETURNED;

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        Library library = new Library(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                library.open(today);
            } else if (command instanceof LibraryCloseCmd) {
                library.close(today);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType();
                LibraryBookIsbn bookIsbn = req.getBookIsbn();
                LibraryBookId bookId = null;
                if (type == QUERIED || type == RETURNED) {
                    bookId = req.getBookId();
                }
                String studentId = req.getStudentId();
                switch (type) {
                    case QUERIED: {
                        library.userQueryBook(bookId, today);
                        break;
                    }
                    case BORROWED: {
                        library.userBorrowBook(bookIsbn, studentId, req);
                        break;
                    }
                    case ORDERED: {
                        library.userOrderBook(bookIsbn, studentId, req);
                        break;
                    }
                    case RETURNED: {
                        library.userReturnBook(bookId, studentId, req);
                        break;
                    }
                    case PICKED: {
                        library.userPickBook(bookIsbn, studentId, req);
                        break;
                    }
                    default: {
                    }
                }
            }
        }
    }
}