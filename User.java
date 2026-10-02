import java.util.Locale;

public class User {

    public static final String ADMIN = "ADMIN";
    public static final String LIBRARIAN = "LIBRARIAN";

    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role.toUpperCase(Locale.ROOT);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username)
                && this.password.equals(password);
    }

    public boolean isAdmin() {
        return ADMIN.equals(role);
    }

    public boolean isLibrarian() {
        return LIBRARIAN.equals(role);
    }

    public static boolean isValidRole(String role) {
        return ADMIN.equalsIgnoreCase(role)
                || LIBRARIAN.equalsIgnoreCase(role);
    }
}