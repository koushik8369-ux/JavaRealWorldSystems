import java.util.Scanner;

public class LibraryManagementSystem {

    static int[] bookIds = new int[5];
    static String[] bookTitles = new String[5];
    static String[] authors = new String[5];
    static boolean[] available = new boolean[5];

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

    public static void issueBook() {

        System.out.print("\nEnter Book ID to issue: ");
        int searchId = sc.nextInt();

        int index = findBook(searchId);

        if (index == -1) {

            System.out.println("Book not found.");

        } else if (!available[index]) {

            System.out.println("Book is already issued.");

        } else {

            available[index] = false;
            System.out.println("Book issued successfully.");
        }
    }

    public static void returnBook() {

        System.out.print("\nEnter Book ID to return: ");
        int searchId = sc.nextInt();

        int index = findBook(searchId);

        if (index == -1) {

            System.out.println("Book not found.");

        } else if (available[index]) {

            System.out.println("Book is already available.");

        } else {

            available[index] = true;
            System.out.println("Book returned successfully.");
        }
    }

    public static void main(String[] args) {

        // Taking book details
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
        }

        int choice;

        while (true) {

            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Display All Books");
            System.out.println("2. Search Book");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Exit");

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
                    issueBook();
                    break;

                case 4:
                    returnBook();
                    break;

                case 5:
                    System.out.println("Thank you for using the Library System.");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}