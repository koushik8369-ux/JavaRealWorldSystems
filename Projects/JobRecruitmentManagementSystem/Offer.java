public class Offer {
    private int offerId;
    private int applicationId;
    private double offeredSalary;
    private String offerDate;
    private String joiningDate;
    private String status;

    public Offer(int offerId, int applicationId, double offeredSalary, String offerDate, String joiningDate, String status) {
        this.offerId = offerId;
        this.applicationId = applicationId;
        this.offeredSalary = offeredSalary;
        this.offerDate = offerDate;
        this.joiningDate = joiningDate;
        this.status = (status == null || status.trim().isEmpty()) ? "Pending" : status;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public double getOfferedSalary() {
        return offeredSalary;
    }

    public void setOfferedSalary(double offeredSalary) {
        this.offeredSalary = offeredSalary;
    }

    public String getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(String offerDate) {
        this.offerDate = offerDate;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.trim().isEmpty()) ? "Pending" : status;
    }

    public void displayDetails() {
        System.out.println("Offer ID        : " + offerId);
        System.out.println("Application ID  : " + applicationId);
        System.out.println("Offered Salary  : ₹" + String.format("%.2f", offeredSalary));
        System.out.println("Offer Date      : " + offerDate);
        System.out.println("Joining Date    : " + joiningDate);
        System.out.println("Status          : " + status);
    }
}
