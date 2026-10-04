public class Application {
    private int applicationId;
    private int candidateId;
    private int jobId;
    private String applicationDate;
    private String status;

    public Application(int applicationId, int candidateId, int jobId, String applicationDate, String status) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.status = (status == null || status.trim().isEmpty()) ? "Applied" : status;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.trim().isEmpty()) ? "Applied" : status;
    }

    public void displayDetails() {
        System.out.println("Application ID : " + applicationId);
        System.out.println("Candidate ID   : " + candidateId);
        System.out.println("Job ID        : " + jobId);
        System.out.println("Application Date: " + applicationDate);
        System.out.println("Status        : " + status);
    }
}
