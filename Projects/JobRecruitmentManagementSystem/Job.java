public class Job {
    private int jobId;
    private String jobTitle;
    private String companyName;
    private String location;
    private String jobType;
    private double salary;

    public Job(int jobId, String jobTitle, String companyName, String location, String jobType, double salary) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.location = location;
        this.jobType = jobType;
        this.salary = salary;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void displayDetails() {
        String formattedSalary = (salary % 1 == 0) ? String.valueOf((long) salary) : String.valueOf(salary);
        System.out.println("Job ID       : " + jobId);
        System.out.println("Job Title    : " + jobTitle);
        System.out.println("Company      : " + companyName);
        System.out.println("Location     : " + location);
        System.out.println("Job Type     : " + jobType);
        System.out.println("Salary       : \u20B9" + formattedSalary);
    }
}
