import java.util.HashMap;

public class UserTable {
    private final HashMap<String, User> users;
    
    public UserTable() {
        this.users = new HashMap<>();
    }

    public User getUser(String studentId) {
        if (!users.containsKey(studentId)) {
            users.put(studentId, new User(studentId));
        }
        return users.get(studentId);
    }
}