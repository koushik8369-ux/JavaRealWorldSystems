public class Transaction {

    private int bookId;
    private int memberId;
    private String type;

    public Transaction(int bookId, int memberId, String type) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.type = type;
    }

    public int getBookId() {
        return bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getType() {
        return type;
    }

    public void displayDetails() {
        System.out.println("Book ID      : " + bookId);
        System.out.println("Member ID    : " + memberId);
        System.out.println("Type         : " + type);
    }
}