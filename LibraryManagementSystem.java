import java.util.Scanner;

public class LibraryManagementSystem {

    static int[] bookIds = new int[5];
    static String[] bookTitles = new String[5];
    static String[] authors = new String[5];
    static boolean[] available = new boolean[5];
    static int[] issuedToMember = new int[5];
    static int[] borrowedDays = new int[5];

    static int[] memberIds = new int[5];
    static String[] memberNames = new String[5];
    static String[] memberPhones = new String[5];

    static int memberCount = 0;
    static int totalFineCollected = 0;

    static final int BORROW_LIMIT = 14;
    static final int FINE_PER_DAY = 5;
    static final int MAX_BOOKS_PER_MEMBER = 2;

    static Scanner sc = new Scanner(System.in);

    public static void displayBooks() {

        System.out.println("\n===== ALL BOOKS =====");

        for (int i = 0; i < 5; i++) {

            System.out.println("\nBook " + (i + 1));
            System.out.println("Book ID      : " + bookIds[i]);
            System.out.println("Book Title   : " + bookTitles[i]);
            System.out.println("Author       : " + authors[i]);
            System.out.println("Availability : "
                    + (available[i] ? "Available" : "Issued"));

            if (!available[i]) {
                System.out.println("Issued To    : Member ID "
                        + issuedToMember[i]);
                System.out.println("Borrowed For : "
                        + borrowedDays[i] + " days");
            }
        }
    }

    public static int findBook(int searchId) {

        for (int i = 0; i < 5; i++) {

            if (bookIds[i] == searchId) {
                return i;
            }
        }

        return -1;
    }

    public static int findMember(int searchId) {

        for (int i = 0; i < memberCount; i++) {

            if (memberIds[i] == searchId) {
                return i;
            }
        }

        return -1;
    }

    public static int countBorrowedBooks(int memberId) {

        int count = 0;

        for (int i = 0; i < 5; i++) {

            if (!available[i] && issuedToMember[i] == memberId) {
                count++;
            }
        }

        return count;
    }

    public static void searchBook() {

        System.out.print("\nEnter Book ID to search: ");
        int searchId = sc.nextInt();

        int index = findBook(searchId);

        if (index != -1) {

            System.out.println("\n===== BOOK FOUND =====");
            System.out.println("Book ID      : " + bookIds[index]);
            System.out.println("Book Title   : " + bookTitles[index]);
            System.out.println("Author       : " + authors[index]);
            System.out.println("Availability : "
                    + (available[index] ? "Available" : "Issued"));

        } else {
            System.out.println("Book not found.");
        }
    }

    public static void registerMember() {

        if (memberCount == 5) {
            System.out.println("Member limit reached.");
            return;
        }

        System.out.println("\n===== REGISTER MEMBER =====");

        System.out.print("Enter Member ID: ");
        int memberId = sc.nextInt();
        sc.nextLine();

        if (findMember(memberId) != -1) {
            System.out.println("Member ID already exists.");
            return;
        }

        System.out.print("Enter Member Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();

        memberIds[memberCount] = memberId;
        memberNames[memberCount] = name;
        memberPhones[memberCount] = phone;

        memberCount++;

        System.out.println("Member registered successfully.");
    }

    public static void displayMembers() {

        if (memberCount == 0) {
            System.out.println("\nNo members registered.");
            return;
        }

        System.out.println("\n===== ALL MEMBERS =====");

        for (int i = 0; i < memberCount; i++) {

            System.out.println("\nMember " + (i + 1));
            System.out.println("Member ID : " + memberIds[i]);
            System.out.println("Name      : " + memberNames[i]);
            System.out.println("Phone     : " + memberPhones[i]);
        }
    }

    public static void issueBook() {

        System.out.print("\nEnter Book ID to issue: ");
        int bookId = sc.nextInt();

        int bookIndex = findBook(bookId);

        if (bookIndex == -1) {
            System.out.println("Book not found.");
            return;
        }

        if (!available[bookIndex]) {
            System.out.println("Book is already issued.");
            return;
        }

        if (memberCount == 0) {
            System.out.println("No members registered.");
            return;
        }

        System.out.print("Enter Member ID: ");
        int memberId = sc.nextInt();

        int memberIndex = findMember(memberId);

        if (memberIndex == -1) {
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

        available[bookIndex] = false;
        issuedToMember[bookIndex] = memberId;
        borrowedDays[bookIndex] = days;

        System.out.println("\nBook issued successfully.");
        System.out.println("Book   : " + bookTitles[bookIndex]);
        System.out.println("Member : " + memberNames[memberIndex]);
        System.out.println("Days   : " + days);
    }

    public static void returnBook() {

        System.out.print("\nEnter Book ID to return: ");
        int bookId = sc.nextInt();

        int bookIndex = findBook(bookId);

        if (bookIndex == -1) {
            System.out.println("Book not found.");
            return;
        }

        if (available[bookIndex]) {
            System.out.println("Book is already available.");
            return;
        }

        System.out.print("Enter actual days kept: ");
        int actualDays = sc.nextInt();

        if (actualDays <= 0) {
            System.out.println("Days must be greater than 0.");
            return;
        }

        int lateDays = actualDays - BORROW_LIMIT;

        if (lateDays < 0) {
            lateDays = 0;
        }

        int fine = lateDays * FINE_PER_DAY;

        System.out.println("\n===== RETURN DETAILS =====");
        System.out.println("Book         : " + bookTitles[bookIndex]);
        System.out.println("Borrowed For : " + borrowedDays[bookIndex]
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

        available[bookIndex] = true;
        issuedToMember[bookIndex] = 0;
        borrowedDays[bookIndex] = 0;

        System.out.println("Book returned successfully.");
    }

    public static void showFineSummary() {

        System.out.println("\n===== FINE SUMMARY =====");
        System.out.println("Total Fine Collected : ₹"
                + totalFineCollected);
    }

    public static void displayMemberBorrowingStatus() {

        System.out.println("\n===== MEMBER BORROWING STATUS =====");

        for (int i = 0; i < memberCount; i++) {

            System.out.println("\nMember ID : " + memberIds[i]);
            System.out.println("Name      : " + memberNames[i]);
            System.out.println("Books     : "
                    + countBorrowedBooks(memberIds[i]) + "/"
                    + MAX_BOOKS_PER_MEMBER);
        }
    }

    public static void main(String[] args) {

        // Add books
        for (int i = 0; i < 5; i++) {

            System.out.println("\nEnter details for Book " + (i + 1));

            System.out.print("Book ID: ");
            bookIds[i] = sc.nextInt();
            sc.nextLine();

            System.out.print("Book Title: ");
            bookTitles[i] = sc.nextLine();

            System.out.print("Author: ");
            authors[i] = sc.nextLine();

            available[i] = true;
            issuedToMember[i] = 0;
            borrowedDays[i] = 0;
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
            System.out.println("9. Exit");

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