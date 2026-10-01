import java.util.ArrayList;
import java.util.Scanner;

public class LibraryManagementSystem {

    private static final ArrayList<Book> books = new ArrayList<>();
    private static final ArrayList<Member> members = new ArrayList<>();
    private static final ArrayList<Transaction> transactions = new ArrayList<>();

    static int totalFineCollected = 0;

    static final int BORROW_LIMIT = 14;
    static final int FINE_PER_DAY = 5;
    static final int MAX_BOOKS_PER_MEMBER = 2;

    static Scanner sc = new Scanner(System.in);

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
        int searchId = sc.nextInt();

        Book book = findBook(searchId);

        if (book != null) {

            System.out.println("\n===== BOOK FOUND =====");
            book.displayDetails();

        } else {
            System.out.println("Book not found.");
        }
    }

    public static void registerMember() {

        System.out.println("\n===== REGISTER MEMBER =====");

        System.out.print("Enter Member ID: ");
        int memberId = sc.nextInt();
        sc.nextLine();

        if (findMember(memberId) != null) {
            System.out.println("Member ID already exists.");
            return;
        }

        System.out.print("Enter Member Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();

        members.add(new Member(memberId, name, phone));

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
        int bookId = sc.nextInt();

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

        System.out.print("Enter Member ID: ");
        int memberId = sc.nextInt();

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

        System.out.print("Enter borrowing days: ");
        int days = sc.nextInt();

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

        System.out.println("\nBook issued successfully.");
        System.out.println("Book   : " + book.getTitle());
        System.out.println("Member : " + member.getName());
        System.out.println("Days   : " + days);
    }

    public static void returnBook() {

        System.out.print("\nEnter Book ID to return: ");
        int bookId = sc.nextInt();

        Book book = findBook(bookId);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (book.isAvailable()) {
            System.out.println("Book is already available.");
            return;
        }

        System.out.print("Enter actual days kept: ");
        int actualDays = sc.nextInt();

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

        System.out.println("Book returned successfully.");
    }

    public static void showFineSummary() {

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

    public static void main(String[] args) {

        // Add the initial book catalog.
        for (int i = 0; i < 5; i++) {

            System.out.println("\nEnter details for Book " + (i + 1));

            int bookId;
            while (true) {
                System.out.print("Book ID: ");
                bookId = sc.nextInt();
                sc.nextLine();

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

        int choice;

        while (true) {

            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Display All Books");
            System.out.println("2. Search Book");
            System.out.println("3. Register Member");
            System.out.println("4. Display Members");
            System.out.println("5. Issue Book");
            System.out.println("6. Return Book");
            System.out.println("7. Fine Summary");
            System.out.println("8. Member Borrowing Status");
            System.out.println("9. Library Statistics");
            System.out.println("10. Transaction History");
            System.out.println("11. Exit");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    displayBooks();
                    break;

                case 2:
                    searchBook();
                    break;

                case 3:
                    registerMember();
                    break;

                case 4:
                    displayMembers();
                    break;

                case 5:
                    issueBook();
                    break;

                case 6:
                    returnBook();
                    break;

                case 7:
                    showFineSummary();
                    break;

                case 8:
                    displayMemberBorrowingStatus();
                    break;

                case 9:
                    displayLibraryStatistics();
                    break;

                case 10:
                    displayTransactionHistory();
                    break;

                case 11:
                    System.out.println(
                            "Thank you for using the Library System.");
                    sc.close();
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again.");
            }
        }
    }
}