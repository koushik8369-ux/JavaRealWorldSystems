import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class LibraryManagementSystem {

    private static final ArrayList<Book> books = new ArrayList<>();
    private static final ArrayList<Member> members = new ArrayList<>();
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final ArrayList<User> users = new ArrayList<>();

    static int totalFineCollected = 0;

    static final int BORROW_LIMIT = 14;
    static final int FINE_PER_DAY = 5;
    static final int MAX_BOOKS_PER_MEMBER = 2;
    static final String BOOK_FILE = "books.txt";
    static final String MEMBER_FILE = "members.txt";
    static final String TRANSACTION_FILE = "transactions.txt";
    static final String ADMIN_FILE = "admin.txt";
    static final String USERS_FILE = "users.txt";

    static Scanner sc = new Scanner(System.in);
    private static boolean saveHadErrors = false;
    static User currentUser;

    private static int readInt(String prompt) {

        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a whole number.");
            }
        }
    }

    private static User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private static void createInitialUsers(File file) {
        ArrayList<User> initialUsers = new ArrayList<>();
        File legacyAdminFile = new File(ADMIN_FILE);

        if (legacyAdminFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(legacyAdminFile))) {
                String line = reader.readLine();
                String[] fields = line == null
                        ? new String[0] : line.split("\\|", -1);
                if (fields.length == 2
                        && !fields[0].isEmpty()
                        && !fields[1].isEmpty()) {
                    initialUsers.add(new User(
                            fields[0], fields[1], User.ADMIN));
                } else {
                    System.out.println(
                            "Error migrating admin credentials: invalid file format.");
                }
            } catch (IOException | SecurityException e) {
                System.out.println("Error migrating admin credentials: "
                        + e.getMessage());
            }
        } else {
            initialUsers.add(new User("admin", "admin123", User.ADMIN));
        }

        if (findInitialUser(initialUsers, "librarian") == null) {
            initialUsers.add(new User("librarian", "lib123", User.LIBRARIAN));
        }

        try {
            if (!file.createNewFile()) {
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file))) {
                for (User user : initialUsers) {
                    writer.write(user.getUsername() + "|"
                            + user.getPassword() + "|" + user.getRole());
                    writer.newLine();
                }
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error creating user credentials: "
                    + e.getMessage());
        }
    }

    private static User findInitialUser(ArrayList<User> initialUsers,
            String username) {
        for (User user : initialUsers) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void loadUsers() {
        users.clear();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            createInitialUsers(file);
        }

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 3
                        || fields[0].isEmpty()
                        || fields[1].isEmpty()
                        || !User.isValidRole(fields[2])
                        || findUser(fields[0]) != null) {
                    System.out.println("Error loading users: invalid record at line "
                            + lineNumber + ".");
                    continue;
                }
                users.add(new User(fields[0], fields[1], fields[2]));
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.write(user.getUsername() + "|"
                        + user.getPassword() + "|" + user.getRole());
                writer.newLine();
            }
        } catch (IOException | SecurityException e) {
            saveHadErrors = true;
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static boolean requireAdmin() {
        if (currentUser == null || !currentUser.isAdmin()) {
            System.out.println("Access denied. Admin privileges required.");
            return false;
        }
        return true;
    }

    public static void displayUsers() {
        if (!requireAdmin()) {
            return;
        }

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        for (User user : users) {
            System.out.println("Username : " + user.getUsername());
            System.out.println("Role     : " + user.getRole());
            System.out.println();
        }
    }

    public static void addUser() {
        if (!requireAdmin()) {
            return;
        }

        System.out.println("Enter username:");
        String username = sc.nextLine().trim();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        System.out.println("Enter role (ADMIN/LIBRARIAN):");
        String role = sc.nextLine().trim().toUpperCase(Locale.ROOT);

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        if (username.contains("|") || password.contains("|")) {
            System.out.println("Username and password cannot contain |.");
            return;
        }
        if (findUser(username) != null) {
            System.out.println("Username already exists.");
            return;
        }
        if (!User.isValidRole(role)) {
            System.out.println("Role must be ADMIN or LIBRARIAN.");
            return;
        }

        users.add(new User(username, password, role));
        saveUsers();
        System.out.println("User added successfully.");
    }

    public static void removeUser() {
        if (!requireAdmin()) {
            return;
        }

        System.out.println("Enter username to remove:");
        String username = sc.nextLine().trim();
        if (currentUser.getUsername().equals(username)) {
            System.out.println("Cannot remove the currently logged-in user.");
            return;
        }

        User user = findUser(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        users.remove(user);
        saveUsers();
        System.out.println("User removed successfully.");
    }

    public static void manageUsers() {
        if (!requireAdmin()) {
            return;
        }

        while (true) {
            System.out.println("\n===== USER MANAGEMENT =====");
            System.out.println("1. Display Users");
            System.out.println("2. Add User");
            System.out.println("3. Remove User");
            System.out.println("4. Back");

            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    displayUsers();
                    break;
                case 2:
                    addUser();
                    break;
                case 3:
                    removeUser();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static boolean login() {

        System.out.println("\n===== LIBRARY LOGIN =====");

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println();
            System.out.println("Username:");
            String username = sc.nextLine().trim();
            System.out.println("Password:");
            String password = sc.nextLine();

            User user = findUser(username);
            if (user != null && user.authenticate(username, password)) {
                currentUser = user;
                System.out.println("Login successful.");
                System.out.println("Welcome, " + user.getUsername() + "!");
                System.out.println("Role: " + user.getRole());
                return true;
            }

            System.out.println("Invalid username or password.");
            if (attempt == 3) {
                System.out.println("Maximum login attempts exceeded.");
                System.out.println("Program terminated.");
                return false;
            }
            System.out.println("Attempts remaining: " + (3 - attempt));
        }

        return false;
    }

    public static void saveBooks() {

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(BOOK_FILE))) {
            for (Book book : books) {
                writer.write(book.getBookId() + "|"
                        + book.getTitle() + "|"
                        + book.getAuthor() + "|"
                        + book.isAvailable() + "|"
                        + book.getIssuedToMember() + "|"
                        + book.getBorrowedDays());
                writer.newLine();
            }
        } catch (IOException | SecurityException e) {
            saveHadErrors = true;
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    public static void saveMembers() {

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(MEMBER_FILE))) {
            for (Member member : members) {
                writer.write(member.getMemberId() + "|"
                        + member.getName() + "|"
                        + member.getPhone());
                writer.newLine();
            }
        } catch (IOException | SecurityException e) {
            saveHadErrors = true;
            System.out.println("Error saving members: " + e.getMessage());
        }
    }

    public static void saveTransactions() {

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(TRANSACTION_FILE))) {
            for (Transaction transaction : transactions) {
                writer.write(transaction.getBookId() + "|"
                        + transaction.getMemberId() + "|"
                        + transaction.getType());
                writer.newLine();
            }
        } catch (IOException | SecurityException e) {
            saveHadErrors = true;
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }

    public static void saveAllData() {
        saveHadErrors = false;
        saveBooks();
        saveMembers();
        saveTransactions();
        saveUsers();
    }

    public static void loadBooks() {

        File file = new File(BOOK_FILE);
        if (!file.exists()) {
            return;
        }

        books.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != 6) {
                        throw new IllegalArgumentException(
                                "invalid record at line " + lineNumber);
                    }

                    int bookId = Integer.parseInt(fields[0]);
                    boolean available;
                    if (fields[3].equalsIgnoreCase("true")) {
                        available = true;
                    } else if (fields[3].equalsIgnoreCase("false")) {
                        available = false;
                    } else {
                        throw new IllegalArgumentException(
                                "invalid availability at line " + lineNumber);
                    }

                    Book book = new Book(bookId, fields[1], fields[2]);
                    book.restoreState(
                            available,
                            Integer.parseInt(fields[4]),
                            Integer.parseInt(fields[5]));

                    if (findBook(bookId) != null) {
                        throw new IllegalArgumentException(
                                "duplicate book ID at line " + lineNumber);
                    }
                    books.add(book);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error loading books: " + e.getMessage());
                }
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    public static void loadMembers() {

        File file = new File(MEMBER_FILE);
        if (!file.exists()) {
            return;
        }

        members.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != 3) {
                        throw new IllegalArgumentException(
                                "invalid record at line " + lineNumber);
                    }

                    int memberId = Integer.parseInt(fields[0]);
                    if (findMember(memberId) != null) {
                        throw new IllegalArgumentException(
                                "duplicate member ID at line " + lineNumber);
                    }
                    members.add(new Member(memberId, fields[1], fields[2]));
                } catch (IllegalArgumentException e) {
                    System.out.println("Error loading members: " + e.getMessage());
                }
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error loading members: " + e.getMessage());
        }
    }

    public static void loadTransactions() {

        File file = new File(TRANSACTION_FILE);
        if (!file.exists()) {
            return;
        }

        transactions.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != 3) {
                        throw new IllegalArgumentException(
                                "invalid record at line " + lineNumber);
                    }
                    if (!fields[2].equals("ISSUE")
                            && !fields[2].equals("RETURN")) {
                        throw new IllegalArgumentException(
                                "invalid transaction type at line " + lineNumber);
                    }

                    transactions.add(new Transaction(
                            Integer.parseInt(fields[0]),
                            Integer.parseInt(fields[1]),
                            fields[2]));
                } catch (IllegalArgumentException e) {
                    System.out.println(
                            "Error loading transactions: " + e.getMessage());
                }
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }

    public static void loadAllData() {
        loadBooks();
        loadMembers();
        loadTransactions();
    }

    public static void displayBooks() {

        System.out.println("\n===== ALL BOOKS =====");

        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            System.out.println("\nBook " + (i + 1));
            book.displayDetails();
        }
    }

    public static Book findBook(int searchId) {

        for (Book book : books) {

            if (book.getBookId() == searchId) {
                return book;
            }
        }

        return null;
    }

    public static Member findMember(int searchId) {

        for (Member member : members) {

            if (member.getMemberId() == searchId) {
                return member;
            }
        }

        return null;
    }

    public static int countBorrowedBooks(int memberId) {

        int count = 0;

        for (Book book : books) {

            if (!book.isAvailable()
                    && book.getIssuedToMember() == memberId) {
                count++;
            }
        }

        return count;
    }

    public static void recordTransaction(
            int bookId,
            int memberId,
            String type) {

        transactions.add(new Transaction(bookId, memberId, type));
    }

    public static void searchBook() {

        System.out.print("\nEnter Book ID to search: ");
        int searchId = readInt("");

        Book book = findBook(searchId);

        if (book != null) {

            System.out.println("\n===== BOOK FOUND =====");
            book.displayDetails();

        } else {
            System.out.println("Book not found.");
        }
    }

    public static void registerMember() {

        if (!requireAdmin()) {
            return;
        }

        System.out.println("\n===== REGISTER MEMBER =====");

        int memberId = readInt("Enter Member ID: ");

        if (findMember(memberId) != null) {
            System.out.println("Member ID already exists.");
            return;
        }

        System.out.print("Enter Member Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();

        members.add(new Member(memberId, name, phone));
        saveAllData();

        System.out.println("Member registered successfully.");
    }

    public static void displayMembers() {

        if (members.isEmpty()) {
            System.out.println("\nNo members registered.");
            return;
        }

        System.out.println("\n===== ALL MEMBERS =====");

        for (int i = 0; i < members.size(); i++) {

            System.out.println("\nMember " + (i + 1));
            members.get(i).displayDetails();
        }
    }

    public static void issueBook() {

        System.out.print("\nEnter Book ID to issue: ");
        int bookId = readInt("");

        Book book = findBook(bookId);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (!book.isAvailable()) {
            System.out.println("Book is already issued.");
            return;
        }

        if (members.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }

        int memberId = readInt("Enter Member ID: ");

        Member member = findMember(memberId);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        if (countBorrowedBooks(memberId) >= MAX_BOOKS_PER_MEMBER) {
            System.out.println("Member already has 2 books.");
            System.out.println("Return a book before borrowing another.");
            return;
        }

        int days = readInt("Enter borrowing days: ");

        if (days <= 0) {
            System.out.println("Borrowing days must be greater than 0.");
            return;
        }

        if (days > BORROW_LIMIT) {
            System.out.println("Maximum borrowing period is "
                    + BORROW_LIMIT + " days.");
            return;
        }

        book.issueTo(memberId, days);
        recordTransaction(bookId, memberId, "ISSUE");
        saveAllData();

        System.out.println("\nBook issued successfully.");
        System.out.println("Book   : " + book.getTitle());
        System.out.println("Member : " + member.getName());
        System.out.println("Days   : " + days);
    }

    public static void returnBook() {

        System.out.print("\nEnter Book ID to return: ");
        int bookId = readInt("");

        Book book = findBook(bookId);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (book.isAvailable()) {
            System.out.println("Book is already available.");
            return;
        }

        int actualDays = readInt("Enter actual days kept: ");

        if (actualDays <= 0) {
            System.out.println("Days must be greater than 0.");
            return;
        }

        int issuedMemberId = book.getIssuedToMember();
        int lateDays = actualDays - BORROW_LIMIT;

        if (lateDays < 0) {
            lateDays = 0;
        }

        int fine = lateDays * FINE_PER_DAY;

        System.out.println("\n===== RETURN DETAILS =====");
        System.out.println("Book         : " + book.getTitle());
        System.out.println("Borrowed For : " + book.getBorrowedDays()
                + " days");
        System.out.println("Actual Days  : " + actualDays);
        System.out.println("Late Days    : " + lateDays);
        System.out.println("Fine         : ₹" + fine);

        if (fine > 0) {
            totalFineCollected += fine;
            System.out.println("Fine collected successfully.");
        } else {
            System.out.println("No fine.");
        }

        book.returnToAvailable();
        recordTransaction(bookId, issuedMemberId, "RETURN");
        saveAllData();

        System.out.println("Book returned successfully.");
    }

    public static void showFineSummary() {

        if (!requireAdmin()) {
            return;
        }

        System.out.println("\n===== FINE SUMMARY =====");
        System.out.println("Total Fine Collected : ₹"
                + totalFineCollected);
    }

    public static void displayMemberBorrowingStatus() {

        System.out.println("\n===== MEMBER BORROWING STATUS =====");

        for (Member member : members) {

            System.out.println("\nMember ID : " + member.getMemberId());
            System.out.println("Name      : " + member.getName());
            System.out.println("Books     : "
                + countBorrowedBooks(member.getMemberId()) + "/"
                    + MAX_BOOKS_PER_MEMBER);
        }
    }

    public static void displayLibraryStatistics() {

        int availableBooks = 0;
        int issuedBooks = 0;

        for (Book book : books) {

            if (book.isAvailable()) {
                availableBooks++;
            } else {
                issuedBooks++;
            }
        }

        System.out.println("\n===== LIBRARY STATISTICS =====");
        System.out.println("Total Books       : " + books.size());
        System.out.println("Available Books   : " + availableBooks);
        System.out.println("Issued Books      : " + issuedBooks);
        System.out.println("Total Members     : " + members.size());
        System.out.println("Total Fine        : ₹" + totalFineCollected);

        System.out.println("\n===== MEMBER BORROWING SUMMARY =====");

        for (Member member : members) {

            System.out.println("\nMember ID : " + member.getMemberId());
            System.out.println("Name      : " + member.getName());
            System.out.println("Books     : "
                + countBorrowedBooks(member.getMemberId()) + "/"
                    + MAX_BOOKS_PER_MEMBER);
        }
    }

    public static void displayTransactionHistory() {

        System.out.println("\n===== TRANSACTION HISTORY =====");

        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded.");
        } else {
            for (int i = 0; i < transactions.size(); i++) {

                System.out.println("\nTransaction " + (i + 1));
                transactions.get(i).displayDetails();
            }
        }

        int totalIssues = 0;
        int totalReturns = 0;

        for (Transaction transaction : transactions) {

            if (transaction.getType().equals("ISSUE")) {
                totalIssues++;
            } else if (transaction.getType().equals("RETURN")) {
                totalReturns++;
            }
        }

        System.out.println("\nTotal Transactions : " + transactions.size());
        System.out.println("Total Issues       : " + totalIssues);
        System.out.println("Total Returns      : " + totalReturns);
    }

    private static void addInitialBooks() {

        for (int i = 0; i < 5; i++) {

            System.out.println("\nEnter details for Book " + (i + 1));

            int bookId;
            while (true) {
                bookId = readInt("Book ID: ");

                if (findBook(bookId) == null) {
                    break;
                }

                System.out.println("Book ID already exists.");
            }

            System.out.print("Book Title: ");
            String title = sc.nextLine();

            System.out.print("Author: ");
            String author = sc.nextLine();

            books.add(new Book(bookId, title, author));
        }
    }

    public static void main(String[] args) {

        loadUsers();
        boolean libraryDataLoaded = false;

        while (true) {

            if (currentUser == null) {
                if (!login()) {
                    sc.close();
                    return;
                }

                if (!libraryDataLoaded) {
                    loadAllData();

                    if (books.isEmpty() && !new File(BOOK_FILE).exists()) {
                        System.out.println("No saved book data found.");
                        addInitialBooks();
                        saveAllData();
                    }

                    libraryDataLoaded = true;
                }
            }

            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Display All Books");
            System.out.println("2. Search Book");
            if (currentUser.isAdmin()) {
                System.out.println("3. Register Member");
                System.out.println("4. Display Members");
                System.out.println("5. Issue Book");
                System.out.println("6. Return Book");
                System.out.println("7. Fine Summary");
                System.out.println("8. Member Borrowing Status");
                System.out.println("9. Library Statistics");
                System.out.println("10. Transaction History");
                System.out.println("11. Save Data");
                System.out.println("12. Manage Users");
                System.out.println("13. Logout");
                System.out.println("14. Exit");
            } else {
                System.out.println("3. Display Members");
                System.out.println("4. Issue Book");
                System.out.println("5. Return Book");
                System.out.println("6. Member Borrowing Status");
                System.out.println("7. Library Statistics");
                System.out.println("8. Transaction History");
                System.out.println("9. Save Data");
                System.out.println("10. Logout");
                System.out.println("11. Exit");
            }

            int choice = readInt("Enter your choice: ");

            switch (choice) {

                case 1:
                    displayBooks();
                    break;

                case 2:
                    searchBook();
                    break;

                case 3:
                    if (currentUser.isAdmin()) {
                        registerMember();
                    } else {
                        displayMembers();
                    }
                    break;

                case 4:
                    if (currentUser.isAdmin()) {
                        displayMembers();
                    } else {
                        issueBook();
                    }
                    break;

                case 5:
                    if (currentUser.isAdmin()) {
                        issueBook();
                    } else {
                        returnBook();
                    }
                    break;

                case 6:
                    if (currentUser.isAdmin()) {
                        returnBook();
                    } else {
                        displayMemberBorrowingStatus();
                    }
                    break;

                case 7:
                    if (currentUser.isAdmin()) {
                        showFineSummary();
                    } else {
                        displayLibraryStatistics();
                    }
                    break;

                case 8:
                    if (currentUser.isAdmin()) {
                        displayMemberBorrowingStatus();
                    } else {
                        displayTransactionHistory();
                    }
                    break;

                case 9:
                    if (currentUser.isAdmin()) {
                        displayLibraryStatistics();
                    } else {
                        saveAllData();
                        if (!saveHadErrors) {
                            System.out.println("Data saved successfully.");
                        }
                    }
                    break;

                case 10:
                    if (currentUser.isAdmin()) {
                        displayTransactionHistory();
                    } else {
                        saveAllData();
                        currentUser = null;
                        System.out.println("Logged out successfully.");
                    }
                    break;

                case 11:
                    if (currentUser.isAdmin()) {
                        saveAllData();
                        if (!saveHadErrors) {
                            System.out.println("Data saved successfully.");
                        }
                    } else {
                        saveAllData();
                        System.out.println("Goodbye!");
                        sc.close();
                        return;
                    }
                    break;

                case 12:
                    if (currentUser.isAdmin()) {
                        manageUsers();
                    } else {
                        System.out.println(
                                "Invalid choice. Please try again.");
                    }
                    break;

                case 13:
                    if (currentUser.isAdmin()) {
                        saveAllData();
                        currentUser = null;
                        System.out.println("Logged out successfully.");
                    } else {
                        System.out.println(
                                "Invalid choice. Please try again.");
                    }
                    break;

                case 14:
                    if (currentUser.isAdmin()) {
                        saveAllData();
                        System.out.println("Goodbye!");
                        sc.close();
                        return;
                    }
                    System.out.println("Invalid choice. Please try again.");
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please try again.");
            }
        }
    }
}