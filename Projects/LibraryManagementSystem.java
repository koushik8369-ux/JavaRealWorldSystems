import java.util.Scanner;

public class LibraryManagementSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int bookId;
        String bookTitle;
        String author;
        boolean available = true;

        System.out.println("===== LIBRARY MANAGEMENT SYSTEM =====");

        System.out.print("Enter Book ID: ");
        bookId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Book Title: ");
        bookTitle = sc.nextLine();

        System.out.print("Enter Author Name: ");
        author = sc.nextLine();

        System.out.println("\n===== BOOK DETAILS =====");
        System.out.println("Book ID      : " + bookId);
        System.out.println("Book Title   : " + bookTitle);
        System.out.println("Author       : " + author);
        System.out.println("Availability : " + (available ? "Available" : "Not Available"));

        System.out.print("\nDo you want to issue the book? (yes/no): ");
        String choice = sc.nextLine();

        if (choice.equalsIgnoreCase("yes")) {

            if (available) {
                available = false;
                System.out.println("Book issued successfully.");
            } else {
                System.out.println("Book is already issued.");
            }

        } else {
            System.out.println("Book was not issued.");
        }

        System.out.println("\n===== UPDATED BOOK STATUS =====");
        System.out.println("Book ID      : " + bookId);
        System.out.println("Book Title   : " + bookTitle);
        System.out.println("Author       : " + author);
        System.out.println("Availability : " + (available ? "Available" : "Not Available"));

        sc.close();
    }
}