public class Company {
    private int companyId;
    private String companyName;
    private String location;
    private String industry;
    private String contactEmail;
    private String contactPhone;

    public Company(int companyId, String companyName, String location, String industry, String contactEmail, String contactPhone) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.location = location;
        this.industry = industry;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void displayDetails() {
        System.out.println("Company ID   : " + companyId);
        System.out.println("Company Name : " + companyName);
        System.out.println("Location     : " + location);
        System.out.println("Industry     : " + industry);
        System.out.println("Contact Email: " + contactEmail);
        System.out.println("Contact Phone: " + contactPhone);
    }
}
