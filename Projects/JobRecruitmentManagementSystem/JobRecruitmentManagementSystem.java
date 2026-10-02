import java.util.ArrayList;
import java.util.Scanner;

public class JobRecruitmentManagementSystem {

    private static ArrayList<Job> jobs = new ArrayList<>();
    private static ArrayList<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            displayMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    registerJob(scanner);
                    break;
                case 2:
                    registerCandidate(scanner);
                    break;
                case 3:
                    displayAllJobs();
                    break;
                case 4:
                    displayAllCandidates();
                    break;
                case 5:
                    searchJob(scanner);
                    break;
                case 6:
                    searchCandidate(scanner);
                    break;
                case 7:
                    System.out.println("Thank you for using Job Recruitment Management System.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 7.");
                    break;
            }
        }

        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n===== JOB RECRUITMENT MANAGEMENT SYSTEM =====\n");
        System.out.println("1. Register Job");
        System.out.println("2. Register Candidate");
        System.out.println("3. Display All Jobs");
        System.out.println("4. Display All Candidates");
        System.out.println("5. Search Job");
        System.out.println("6. Search Candidate");
        System.out.println("7. Exit");
    }

    private static void registerJob(Scanner scanner) {
        System.out.print("Enter Job ID: ");
        String jobIdStr = scanner.nextLine().trim();
        int jobId;
        try {
            jobId = Integer.parseInt(jobIdStr);
            if (jobId <= 0) {
                System.out.println("Invalid Job ID. Job ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Job ID. Please enter a valid number.");
            return;
        }

        if (findJobById(jobId) != null) {
            System.out.println("Job ID already exists.");
            return;
        }

        System.out.print("Enter Job Title: ");
        String jobTitle = scanner.nextLine().trim();
        if (jobTitle.isEmpty()) {
            System.out.println("Job title cannot be empty.");
            return;
        }

        System.out.print("Enter Company Name: ");
        String companyName = scanner.nextLine().trim();
        if (companyName.isEmpty()) {
            System.out.println("Company name cannot be empty.");
            return;
        }

        System.out.print("Enter Location: ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            System.out.println("Location cannot be empty.");
            return;
        }

        System.out.print("Enter Job Type: ");
        String jobType = scanner.nextLine().trim();
        if (jobType.isEmpty()) {
            System.out.println("Job type cannot be empty.");
            return;
        }

        System.out.print("Enter Salary: ");
        String salaryStr = scanner.nextLine().trim();
        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
            if (salary <= 0) {
                System.out.println("Invalid salary. Salary must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid salary. Please enter a valid number.");
            return;
        }

        jobs.add(new Job(jobId, jobTitle, companyName, location, jobType, salary));
        System.out.println("Job registered successfully.");
    }

    private static void registerCandidate(Scanner scanner) {
        System.out.print("Enter Candidate ID: ");
        String candidateIdStr = scanner.nextLine().trim();
        int candidateId;
        try {
            candidateId = Integer.parseInt(candidateIdStr);
            if (candidateId <= 0) {
                System.out.println("Invalid Candidate ID. Candidate ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Candidate ID. Please enter a valid number.");
            return;
        }

        if (findCandidateById(candidateId) != null) {
            System.out.println("Candidate ID already exists.");
            return;
        }

        System.out.print("Enter Candidate Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Enter Age: ");
        String ageStr = scanner.nextLine().trim();
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0) {
                System.out.println("Invalid age. Age must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Please enter a valid number.");
            return;
        }

        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return;
        }

        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            System.out.println("Phone cannot be empty.");
            return;
        }

        System.out.print("Enter Skills: ");
        String skills = scanner.nextLine().trim();
        if (skills.isEmpty()) {
            System.out.println("Skills cannot be empty.");
            return;
        }

        candidates.add(new Candidate(candidateId, name, age, email, phone, skills));
        System.out.println("Candidate registered successfully.");
    }

    private static void displayAllJobs() {
        if (jobs.isEmpty()) {
            System.out.println("No jobs registered.");
            return;
        }

        System.out.println("\n===== JOB LIST =====");
        for (Job job : jobs) {
            System.out.println();
            job.displayDetails();
        }
    }

    private static void displayAllCandidates() {
        if (candidates.isEmpty()) {
            System.out.println("No candidates registered.");
            return;
        }

        System.out.println("\n===== CANDIDATE LIST =====");
        for (Candidate candidate : candidates) {
            System.out.println();
            candidate.displayDetails();
        }
    }

    private static void searchJob(Scanner scanner) {
        System.out.print("Enter Job ID: ");
        String jobIdStr = scanner.nextLine().trim();
        int jobId;
        try {
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Job ID. Please enter a valid number.");
            return;
        }

        Job job = findJobById(jobId);
        if (job != null) {
            System.out.println();
            job.displayDetails();
        } else {
            System.out.println("Job not found.");
        }
    }

    private static void searchCandidate(Scanner scanner) {
        System.out.print("Enter Candidate ID: ");
        String candidateIdStr = scanner.nextLine().trim();
        int candidateId;
        try {
            candidateId = Integer.parseInt(candidateIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Candidate ID. Please enter a valid number.");
            return;
        }

        Candidate candidate = findCandidateById(candidateId);
        if (candidate != null) {
            System.out.println();
            candidate.displayDetails();
        } else {
            System.out.println("Candidate not found.");
        }
    }

    private static Job findJobById(int jobId) {
        for (Job job : jobs) {
            if (job.getJobId() == jobId) {
                return job;
            }
        }
        return null;
    }

    private static Candidate findCandidateById(int candidateId) {
        for (Candidate candidate : candidates) {
            if (candidate.getCandidateId() == candidateId) {
                return candidate;
            }
        }
        return null;
    }
}
