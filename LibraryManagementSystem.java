import java.util.Scanner;

public class LibraryManagementSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int[] bookIds = new int[5];
        String[] bookTitles = new String[5];
        String[] authors = new String[5];
        boolean[] available = new boolean[5];

        System.out.println("===== LIBRARY MANAGEMENT SYSTEM =====");

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

        // Display all books
        System.out.println("\n===== ALL BOOKS =====");

        for (int i = 0; i < 5; i++) {

            System.out.println("\nBook " + (i + 1));
            System.out.println("Book ID      : " + bookIds[i]);
            System.out.println("Book Title   : " + bookTitles[i]);
            System.out.println("Author       : " + authors[i]);
            System.out.println("Availability : "
                    + (available[i] ? "Available" : "Not Available"));
        }

        // Search book
        System.out.print("\nEnter Book ID to search: ");
        int searchId = sc.nextInt();

        boolean found = false;

        for (int i = 0; i < 5; i++) {

            if (bookIds[i] == searchId) {

                System.out.println("\n===== BOOK FOUND =====");
                System.out.println("Book ID      : " + bookIds[i]);
                System.out.println("Book Title   : " + bookTitles[i]);
                System.out.println("Author       : " + authors[i]);
                System.out.println("Availability : "
                        + (available[i] ? "Available" : "Not Available"));

                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Book not found.");
        }

        sc.close();
    }
}