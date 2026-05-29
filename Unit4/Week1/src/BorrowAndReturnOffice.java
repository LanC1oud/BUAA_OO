import java.util.HashSet;

public class BorrowAndReturnOffice {
    private final HashSet<Book> books;
    
    public BorrowAndReturnOffice() {
        books = new HashSet<>();
    }
    
    public void addBook(Book book) {
        books.add(book);
    }
    
    public HashSet<Book> removeAllBooks() {
        HashSet<Book> temp = new HashSet<>(books);
        books.clear();
        return temp;
    }
}