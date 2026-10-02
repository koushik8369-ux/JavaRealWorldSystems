public class Recruiter {
    private int recruiterId;
    private String recruiterName;
    private String companyName;
    private String email;
    private String phone;

    public Recruiter(int recruiterId, String recruiterName, String companyName, String email, String phone) {
        this.recruiterId = recruiterId;
        this.recruiterName = recruiterName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
    }

    public int getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(int recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void displayDetails() {
        System.out.println("Recruiter ID : " + recruiterId);
        System.out.println("Name         : " + recruiterName);
        System.out.println("Company      : " + companyName);
        System.out.println("Email        : " + email);
        System.out.println("Phone        : " + phone);
    }
}
