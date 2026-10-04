public class Interview {
    private int interviewId;
    private int applicationId;
    private String interviewDate;
    private String interviewTime;
    private String interviewMode;
    private String interviewer;
    private String status;
    private String result;

    public Interview(int interviewId, int applicationId, String interviewDate, String interviewTime,
                     String interviewMode, String interviewer, String status, String result) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.interviewMode = interviewMode;
        this.interviewer = interviewer;
        this.status = (status == null || status.trim().isEmpty()) ? "Scheduled" : status;
        this.result = (result == null || result.trim().isEmpty()) ? "Pending" : result;
    }

    public int getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(int interviewId) {
        this.interviewId = interviewId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.trim().isEmpty()) ? "Scheduled" : status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = (result == null || result.trim().isEmpty()) ? "Pending" : result;
    }

    public void displayDetails() {
        System.out.println("Interview ID    : " + interviewId);
        System.out.println("Application ID  : " + applicationId);
        System.out.println("Interview Date  : " + interviewDate);
        System.out.println("Interview Time  : " + interviewTime);
        System.out.println("Interview Mode  : " + interviewMode);
        System.out.println("Interviewer     : " + interviewer);
        System.out.println("Status          : " + status);
        System.out.println("Result          : " + result);
    }
}
