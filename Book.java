public class Book {

    private int bookId;
    private String title;
    private String author;
    private boolean available;
    private int issuedToMember;
    private int borrowedDays;

    public Book(int bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.available = true;
        this.issuedToMember = 0;
        this.borrowedDays = 0;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getIssuedToMember() {
        return issuedToMember;
    }

    public int getBorrowedDays() {
        return borrowedDays;
    }

    public void issueTo(int memberId, int days) {
        available = false;
        issuedToMember = memberId;
        borrowedDays = days;
    }

    public void returnToAvailable() {
        available = true;
        issuedToMember = 0;
        borrowedDays = 0;
    }

    public void restoreState(
            boolean available,
            int issuedToMember,
            int borrowedDays) {
        this.available = available;
        this.issuedToMember = issuedToMember;
        this.borrowedDays = borrowedDays;
    }

    public void displayDetails() {
        System.out.println("Book ID      : " + bookId);
        System.out.println("Book Title   : " + title);
        System.out.println("Author       : " + author);
        System.out.println("Availability : "
                + (available ? "Available" : "Issued"));

        if (!available) {
            System.out.println("Issued To    : Member ID " + issuedToMember);
            System.out.println("Borrowed For : " + borrowedDays + " days");
        }
    }
}