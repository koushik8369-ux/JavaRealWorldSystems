import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class JobRecruitmentManagementSystem {

    private static ArrayList<Company> companies = new ArrayList<>();
    private static ArrayList<Recruiter> recruiters = new ArrayList<>();
    private static ArrayList<Job> jobs = new ArrayList<>();
    private static ArrayList<Candidate> candidates = new ArrayList<>();
    private static ArrayList<Application> applications = new ArrayList<>();
    private static ArrayList<Interview> interviews = new ArrayList<>();

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
                    registerCompany(scanner);
                    break;
                case 2:
                    registerRecruiter(scanner);
                    break;
                case 3:
                    registerJob(scanner);
                    break;
                case 4:
                    registerCandidate(scanner);
                    break;
                case 5:
                    displayAllCompanies();
                    break;
                case 6:
                    displayAllRecruiters();
                    break;
                case 7:
                    displayAllJobs();
                    break;
                case 8:
                    displayAllCandidates();
                    break;
                case 9:
                    searchCompany(scanner);
                    break;
                case 10:
                    searchRecruiter(scanner);
                    break;
                case 11:
                    searchJob(scanner);
                    break;
                case 12:
                    searchCandidate(scanner);
                    break;
                case 13:
                    advancedJobSearchMenu(scanner);
                    break;
                case 14:
                    advancedCandidateSearchMenu(scanner);
                    break;
                case 15:
                    applyForJob(scanner);
                    break;
                case 16:
                    displayAllApplications();
                    break;
                case 17:
                    searchApplicationsByCandidate(scanner);
                    break;
                case 18:
                    searchApplicationsByJob(scanner);
                    break;
                case 19:
                    searchApplicationById(scanner);
                    break;
                case 20:
                    updateApplicationStatus(scanner);
                    break;
                case 21:
                    scheduleInterview(scanner);
                    break;
                case 22:
                    displayAllInterviews();
                    break;
                case 23:
                    searchInterviewById(scanner);
                    break;
                case 24:
                    searchInterviewsByCandidate(scanner);
                    break;
                case 25:
                    searchInterviewsByJob(scanner);
                    break;
                case 26:
                    updateInterviewStatus(scanner);
                    break;
                case 27:
                    recordInterviewResult(scanner);
                    break;
                case 28:
                    System.out.println("Thank you for using Job Recruitment Management System.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 28.");
                    break;
            }
        }

        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n===== JOB RECRUITMENT MANAGEMENT SYSTEM =====\n");
        System.out.println("1. Register Company");
        System.out.println("2. Register Recruiter");
        System.out.println("3. Register Job");
        System.out.println("4. Register Candidate");
        System.out.println("5. Display All Companies");
        System.out.println("6. Display All Recruiters");
        System.out.println("7. Display All Jobs");
        System.out.println("8. Display All Candidates");
        System.out.println("9. Search Company");
        System.out.println("10. Search Recruiter");
        System.out.println("11. Search Job by ID");
        System.out.println("12. Search Candidate by ID");
        System.out.println("13. Advanced Job Search");
        System.out.println("14. Advanced Candidate Search");
        System.out.println("15. Apply for Job");
        System.out.println("16. Display All Applications");
        System.out.println("17. Search Applications by Candidate");
        System.out.println("18. Search Applications by Job");
        System.out.println("19. Search Application by ID");
        System.out.println("20. Update Application Status");
        System.out.println("21. Schedule Interview");
        System.out.println("22. Display All Interviews");
        System.out.println("23. Search Interview by ID");
        System.out.println("24. Search Interviews by Candidate");
        System.out.println("25. Search Interviews by Job");
        System.out.println("26. Update Interview Status");
        System.out.println("27. Record Interview Result");
        System.out.println("28. Exit");
    }

    private static void registerCompany(Scanner scanner) {
        System.out.print("Enter Company ID: ");
        String companyIdStr = scanner.nextLine().trim();
        int companyId;
        try {
            companyId = Integer.parseInt(companyIdStr);
            if (companyId <= 0) {
                System.out.println("Invalid Company ID. Company ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Company ID. Please enter a valid number.");
            return;
        }

        if (findCompanyById(companyId) != null) {
            System.out.println("Company ID already exists.");
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

        System.out.print("Enter Industry: ");
        String industry = scanner.nextLine().trim();
        if (industry.isEmpty()) {
            System.out.println("Industry cannot be empty.");
            return;
        }

        System.out.print("Enter Contact Email: ");
        String contactEmail = scanner.nextLine().trim();
        if (contactEmail.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return;
        }

        System.out.print("Enter Contact Phone: ");
        String contactPhone = scanner.nextLine().trim();
        if (contactPhone.isEmpty()) {
            System.out.println("Phone cannot be empty.");
            return;
        }

        companies.add(new Company(companyId, companyName, location, industry, contactEmail, contactPhone));
        System.out.println("Company registered successfully.");
    }

    private static void registerRecruiter(Scanner scanner) {
        System.out.print("Enter Recruiter ID: ");
        String recruiterIdStr = scanner.nextLine().trim();
        int recruiterId;
        try {
            recruiterId = Integer.parseInt(recruiterIdStr);
            if (recruiterId <= 0) {
                System.out.println("Invalid Recruiter ID. Recruiter ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Recruiter ID. Please enter a valid number.");
            return;
        }

        if (findRecruiterById(recruiterId) != null) {
            System.out.println("Recruiter ID already exists.");
            return;
        }

        System.out.print("Enter Recruiter Name: ");
        String recruiterName = scanner.nextLine().trim();
        if (recruiterName.isEmpty()) {
            System.out.println("Recruiter name cannot be empty.");
            return;
        }

        System.out.print("Enter Company Name: ");
        String companyName = scanner.nextLine().trim();
        if (companyName.isEmpty()) {
            System.out.println("Company name cannot be empty.");
            return;
        }

        Company company = findCompanyByName(companyName);
        if (company == null) {
            System.out.println("Company not found. Please register the company first.");
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

        recruiters.add(new Recruiter(recruiterId, recruiterName, company.getCompanyName(), email, phone));
        System.out.println("Recruiter registered successfully.");
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

        Company company = findCompanyByName(companyName);
        if (company == null) {
            System.out.println("Company not found. Please register the company first.");
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

        jobs.add(new Job(jobId, jobTitle, company.getCompanyName(), location, jobType, salary));
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

    private static void displayAllCompanies() {
        if (companies.isEmpty()) {
            System.out.println("No companies registered.");
            return;
        }

        System.out.println("\n===== COMPANY LIST =====");
        for (Company company : companies) {
            System.out.println();
            company.displayDetails();
        }
    }

    private static void displayAllRecruiters() {
        if (recruiters.isEmpty()) {
            System.out.println("No recruiters registered.");
            return;
        }

        System.out.println("\n===== RECRUITER LIST =====");
        for (Recruiter recruiter : recruiters) {
            System.out.println();
            recruiter.displayDetails();
        }
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

    private static void searchCompany(Scanner scanner) {
        System.out.print("Enter Company ID: ");
        String companyIdStr = scanner.nextLine().trim();
        int companyId;
        try {
            companyId = Integer.parseInt(companyIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Company ID. Please enter a valid number.");
            return;
        }

        Company company = findCompanyById(companyId);
        if (company != null) {
            System.out.println();
            company.displayDetails();
        } else {
            System.out.println("Company not found.");
        }
    }

    private static void searchRecruiter(Scanner scanner) {
        System.out.print("Enter Recruiter ID: ");
        String recruiterIdStr = scanner.nextLine().trim();
        int recruiterId;
        try {
            recruiterId = Integer.parseInt(recruiterIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Recruiter ID. Please enter a valid number.");
            return;
        }

        Recruiter recruiter = findRecruiterById(recruiterId);
        if (recruiter != null) {
            System.out.println();
            recruiter.displayDetails();
        } else {
            System.out.println("Recruiter not found.");
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

    private static void advancedJobSearchMenu(Scanner scanner) {
        boolean inSubMenu = true;
        while (inSubMenu) {
            System.out.println("\n===== ADVANCED JOB SEARCH =====\n");
            System.out.println("1. Search by Job Title");
            System.out.println("2. Search by Company");
            System.out.println("3. Search by Location");
            System.out.println("4. Filter by Job Type");
            System.out.println("5. Filter by Maximum Salary");
            System.out.println("6. Combined Search");
            System.out.println("7. Back");
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
                    searchByJobTitle(scanner);
                    break;
                case 2:
                    searchByCompany(scanner);
                    break;
                case 3:
                    searchByLocation(scanner);
                    break;
                case 4:
                    filterByJobType(scanner);
                    break;
                case 5:
                    filterByMaxSalary(scanner);
                    break;
                case 6:
                    combinedJobSearch(scanner);
                    break;
                case 7:
                    inSubMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 7.");
                    break;
            }
        }
    }

    private static void searchByJobTitle(Scanner scanner) {
        System.out.print("Enter Job Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Job title cannot be empty.");
            return;
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getJobTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(job);
            }
        }
        displayJobResults(results);
    }

    private static void searchByCompany(Scanner scanner) {
        System.out.print("Enter Company Name: ");
        String company = scanner.nextLine().trim();
        if (company.isEmpty()) {
            System.out.println("Company name cannot be empty.");
            return;
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getCompanyName().toLowerCase().contains(company.toLowerCase())) {
                results.add(job);
            }
        }
        displayJobResults(results);
    }

    private static void searchByLocation(Scanner scanner) {
        System.out.print("Enter Location: ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            System.out.println("Location cannot be empty.");
            return;
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getLocation().toLowerCase().contains(location.toLowerCase())) {
                results.add(job);
            }
        }
        displayJobResults(results);
    }

    private static void filterByJobType(Scanner scanner) {
        System.out.print("Enter Job Type: ");
        String jobType = scanner.nextLine().trim();
        if (jobType.isEmpty()) {
            System.out.println("Job type cannot be empty.");
            return;
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getJobType().toLowerCase().contains(jobType.toLowerCase())) {
                results.add(job);
            }
        }
        displayJobResults(results);
    }

    private static void filterByMaxSalary(Scanner scanner) {
        System.out.print("Enter Maximum Salary: ");
        String salaryStr = scanner.nextLine().trim();
        double maxSalary;
        try {
            maxSalary = Double.parseDouble(salaryStr);
            if (maxSalary <= 0) {
                System.out.println("Invalid salary.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid salary.");
            return;
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getSalary() <= maxSalary) {
                results.add(job);
            }
        }
        displayJobResults(results);
    }

    private static void combinedJobSearch(Scanner scanner) {
        System.out.println("\n===== COMBINED JOB SEARCH =====\n");
        System.out.print("Enter Job Title (press Enter to skip): ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Company (press Enter to skip): ");
        String company = scanner.nextLine().trim();

        System.out.print("Enter Location (press Enter to skip): ");
        String location = scanner.nextLine().trim();

        System.out.print("Enter Job Type (press Enter to skip): ");
        String jobType = scanner.nextLine().trim();

        System.out.print("Enter Maximum Salary (press Enter to skip): ");
        String salaryStr = scanner.nextLine().trim();

        boolean hasMaxSalary = false;
        double maxSalary = 0;
        if (!salaryStr.isEmpty()) {
            try {
                maxSalary = Double.parseDouble(salaryStr);
                if (maxSalary <= 0) {
                    System.out.println("Invalid salary.");
                    return;
                }
                hasMaxSalary = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid salary.");
                return;
            }
        }

        ArrayList<Job> results = new ArrayList<>();
        for (Job job : jobs) {
            if (!title.isEmpty() && !job.getJobTitle().toLowerCase().contains(title.toLowerCase())) {
                continue;
            }
            if (!company.isEmpty() && !job.getCompanyName().toLowerCase().contains(company.toLowerCase())) {
                continue;
            }
            if (!location.isEmpty() && !job.getLocation().toLowerCase().contains(location.toLowerCase())) {
                continue;
            }
            if (!jobType.isEmpty() && !job.getJobType().toLowerCase().contains(jobType.toLowerCase())) {
                continue;
            }
            if (hasMaxSalary && job.getSalary() > maxSalary) {
                continue;
            }
            results.add(job);
        }
        displayJobResults(results);
    }

    private static void displayJobResults(ArrayList<Job> results) {
        if (results.isEmpty()) {
            System.out.println("No matching jobs found.");
            return;
        }

        System.out.println("\n===== SEARCH RESULTS =====");
        for (Job job : results) {
            System.out.println();
            job.displayDetails();
        }
    }

    private static void advancedCandidateSearchMenu(Scanner scanner) {
        boolean inSubMenu = true;
        while (inSubMenu) {
            System.out.println("\n===== ADVANCED CANDIDATE SEARCH =====\n");
            System.out.println("1. Search by Name");
            System.out.println("2. Search by Skill");
            System.out.println("3. Search by Email");
            System.out.println("4. Filter by Maximum Age");
            System.out.println("5. Combined Search");
            System.out.println("6. Back");
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
                    searchCandidateByName(scanner);
                    break;
                case 2:
                    searchCandidateBySkill(scanner);
                    break;
                case 3:
                    searchCandidateByEmail(scanner);
                    break;
                case 4:
                    filterCandidateByMaxAge(scanner);
                    break;
                case 5:
                    combinedCandidateSearch(scanner);
                    break;
                case 6:
                    inSubMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 6.");
                    break;
            }
        }
    }

    private static void searchCandidateByName(Scanner scanner) {
        System.out.print("Enter Candidate Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Candidate name cannot be empty.");
            return;
        }

        ArrayList<Candidate> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getName().toLowerCase().contains(name.toLowerCase())) {
                results.add(candidate);
            }
        }
        displayCandidateResults(results);
    }

    private static void searchCandidateBySkill(Scanner scanner) {
        System.out.print("Enter Skill: ");
        String skill = scanner.nextLine().trim();
        if (skill.isEmpty()) {
            System.out.println("Skill cannot be empty.");
            return;
        }

        ArrayList<Candidate> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getSkills().toLowerCase().contains(skill.toLowerCase())) {
                results.add(candidate);
            }
        }
        displayCandidateResults(results);
    }

    private static void searchCandidateByEmail(Scanner scanner) {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return;
        }

        ArrayList<Candidate> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getEmail().toLowerCase().contains(email.toLowerCase())) {
                results.add(candidate);
            }
        }
        displayCandidateResults(results);
    }

    private static void filterCandidateByMaxAge(Scanner scanner) {
        System.out.print("Enter Maximum Age: ");
        String ageStr = scanner.nextLine().trim();
        int maxAge;
        try {
            maxAge = Integer.parseInt(ageStr);
            if (maxAge <= 0) {
                System.out.println("Invalid age.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid age.");
            return;
        }

        ArrayList<Candidate> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (candidate.getAge() <= maxAge) {
                results.add(candidate);
            }
        }
        displayCandidateResults(results);
    }

    private static void combinedCandidateSearch(Scanner scanner) {
        System.out.println("\n===== COMBINED CANDIDATE SEARCH =====\n");
        System.out.print("Enter Name (press Enter to skip): ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Skill (press Enter to skip): ");
        String skill = scanner.nextLine().trim();

        System.out.print("Enter Email (press Enter to skip): ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Maximum Age (press Enter to skip): ");
        String ageStr = scanner.nextLine().trim();

        boolean hasMaxAge = false;
        int maxAge = 0;
        if (!ageStr.isEmpty()) {
            try {
                maxAge = Integer.parseInt(ageStr);
                if (maxAge <= 0) {
                    System.out.println("Invalid age.");
                    return;
                }
                hasMaxAge = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid age.");
                return;
            }
        }

        ArrayList<Candidate> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (!name.isEmpty() && !candidate.getName().toLowerCase().contains(name.toLowerCase())) {
                continue;
            }
            if (!skill.isEmpty() && !candidate.getSkills().toLowerCase().contains(skill.toLowerCase())) {
                continue;
            }
            if (!email.isEmpty() && !candidate.getEmail().toLowerCase().contains(email.toLowerCase())) {
                continue;
            }
            if (hasMaxAge && candidate.getAge() > maxAge) {
                continue;
            }
            results.add(candidate);
        }
        displayCandidateResults(results);
    }

    private static void displayCandidateResults(ArrayList<Candidate> results) {
        if (results.isEmpty()) {
            System.out.println("No matching candidates found.");
            return;
        }

        System.out.println("\n===== SEARCH RESULTS =====");
        for (Candidate candidate : results) {
            System.out.println();
            candidate.displayDetails();
        }
    }

    private static void applyForJob(Scanner scanner) {
        System.out.println("\n===== APPLY FOR JOB =====");
        System.out.print("Enter Application ID: ");
        String applicationIdStr = scanner.nextLine().trim();
        int applicationId;
        try {
            applicationId = Integer.parseInt(applicationIdStr);
            if (applicationId <= 0) {
                System.out.println("Invalid Application ID. Application ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID. Please enter a valid number.");
            return;
        }

        if (findApplicationById(applicationId) != null) {
            System.out.println("Application ID already exists.");
            return;
        }

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
        if (candidate == null) {
            System.out.println("Candidate not found.");
            return;
        }

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
        if (job == null) {
            System.out.println("Job not found.");
            return;
        }

        if (findApplicationByCandidateAndJob(candidateId, jobId) != null) {
            System.out.println("You have already applied for this job.");
            return;
        }

        applications.add(new Application(applicationId, candidateId, jobId, LocalDate.now().toString(), "Applied"));
        System.out.println("Application submitted successfully.");
    }

    private static void displayAllApplications() {
        if (applications.isEmpty()) {
            System.out.println("No applications found.");
            return;
        }

        System.out.println("\n===== APPLICATION LIST =====");
        for (Application application : applications) {
            System.out.println();
            application.displayDetails();
        }
    }

    private static void searchApplicationsByCandidate(Scanner scanner) {
        System.out.print("Enter Candidate ID: ");
        String candidateIdStr = scanner.nextLine().trim();
        int candidateId;
        try {
            candidateId = Integer.parseInt(candidateIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Candidate ID. Please enter a valid number.");
            return;
        }

        ArrayList<Application> results = new ArrayList<>();
        for (Application application : applications) {
            if (application.getCandidateId() == candidateId) {
                results.add(application);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No applications found for candidate ID: " + candidateId + ".");
            return;
        }

        System.out.println("\n===== APPLICATION SEARCH RESULTS =====");
        for (Application application : results) {
            System.out.println();
            application.displayDetails();
        }
    }

    private static void searchApplicationsByJob(Scanner scanner) {
        System.out.print("Enter Job ID: ");
        String jobIdStr = scanner.nextLine().trim();
        int jobId;
        try {
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Job ID. Please enter a valid number.");
            return;
        }

        ArrayList<Application> results = new ArrayList<>();
        for (Application application : applications) {
            if (application.getJobId() == jobId) {
                results.add(application);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No applications found for job ID: " + jobId + ".");
            return;
        }

        System.out.println("\n===== APPLICATION SEARCH RESULTS =====");
        for (Application application : results) {
            System.out.println();
            application.displayDetails();
        }
    }

    private static void searchApplicationById(Scanner scanner) {
        System.out.print("Enter Application ID: ");
        String applicationIdStr = scanner.nextLine().trim();
        int applicationId;
        try {
            applicationId = Integer.parseInt(applicationIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID. Please enter a valid number.");
            return;
        }

        Application application = findApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        System.out.println();
        application.displayDetails();
    }

    private static void updateApplicationStatus(Scanner scanner) {
        System.out.print("Enter Application ID: ");
        String applicationIdStr = scanner.nextLine().trim();
        int applicationId;
        try {
            applicationId = Integer.parseInt(applicationIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID. Please enter a valid number.");
            return;
        }

        Application application = findApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        System.out.print("Enter new status: ");
        String status = scanner.nextLine().trim();
        if (!isValidApplicationStatus(status)) {
            System.out.println("Invalid status. Allowed statuses: Applied, Under Review, Shortlisted, Rejected, Selected");
            return;
        }

        application.setStatus(status);
        System.out.println("Application status updated successfully.");
    }

    private static boolean isValidApplicationStatus(String status) {
        return status != null && (status.equals("Applied")
                || status.equals("Under Review")
                || status.equals("Shortlisted")
                || status.equals("Rejected")
                || status.equals("Selected"));
    }

    private static Application findApplicationById(int applicationId) {
        for (Application application : applications) {
            if (application.getApplicationId() == applicationId) {
                return application;
            }
        }
        return null;
    }

    private static Application findApplicationByCandidateAndJob(int candidateId, int jobId) {
        for (Application application : applications) {
            if (application.getCandidateId() == candidateId && application.getJobId() == jobId) {
                return application;
            }
        }
        return null;
    }

    private static void scheduleInterview(Scanner scanner) {
        System.out.println("\n===== SCHEDULE INTERVIEW =====");

        System.out.print("Enter Interview ID: ");
        String interviewIdStr = scanner.nextLine().trim();
        int interviewId;
        try {
            interviewId = Integer.parseInt(interviewIdStr);
            if (interviewId <= 0) {
                System.out.println("Invalid Interview ID. Interview ID must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Interview ID. Please enter a valid number.");
            return;
        }

        if (findInterviewById(interviewId) != null) {
            System.out.println("Interview ID already exists.");
            return;
        }

        System.out.print("Enter Application ID: ");
        String applicationIdStr = scanner.nextLine().trim();
        int applicationId;
        try {
            applicationId = Integer.parseInt(applicationIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID. Please enter a valid number.");
            return;
        }

        Application application = findApplicationById(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        if (!application.getStatus().equals("Shortlisted")) {
            System.out.println("Only shortlisted candidates can be scheduled for an interview.");
            return;
        }

        for (Interview interview : interviews) {
            if (interview.getApplicationId() == applicationId && interview.getStatus().equals("Scheduled")) {
                System.out.println("An interview is already scheduled for this application.");
                return;
            }
        }

        System.out.print("Enter Interview Date (YYYY-MM-DD): ");
        String interviewDate = scanner.nextLine().trim();
        try {
            LocalDate.parse(interviewDate);
        } catch (Exception e) {
            System.out.println("Invalid interview date.");
            return;
        }

        System.out.print("Enter Interview Time (HH:MM): ");
        String interviewTime = scanner.nextLine().trim();
        try {
            LocalTime.parse(interviewTime);
        } catch (Exception e) {
            System.out.println("Invalid interview time.");
            return;
        }

        System.out.println("Select Interview Mode:");
        System.out.println("1. Online");
        System.out.println("2. Offline");
        System.out.println("3. Phone");
        System.out.print("Enter choice: ");
        String modeChoice = scanner.nextLine().trim();
        String interviewMode;
        switch (modeChoice) {
            case "1":
                interviewMode = "Online";
                break;
            case "2":
                interviewMode = "Offline";
                break;
            case "3":
                interviewMode = "Phone";
                break;
            default:
                System.out.println("Invalid interview mode.");
                return;
        }

        System.out.print("Enter Interviewer Name: ");
        String interviewer = scanner.nextLine().trim();
        if (interviewer.isEmpty()) {
            System.out.println("Invalid interviewer name.");
            return;
        }

        interviews.add(new Interview(interviewId, applicationId, interviewDate, interviewTime, interviewMode, interviewer, "Scheduled", "Pending"));
        System.out.println("Interview scheduled successfully.");
        Interview interview = findInterviewById(interviewId);
        if (interview != null) {
            interview.displayDetails();
        }
    }

    private static void displayAllInterviews() {
        if (interviews.isEmpty()) {
            System.out.println("No interviews found.");
            return;
        }

        System.out.println("\n===== INTERVIEW LIST =====");
        for (Interview interview : interviews) {
            System.out.println();
            interview.displayDetails();
        }
    }

    private static void searchInterviewById(Scanner scanner) {
        System.out.print("Enter Interview ID: ");
        String interviewIdStr = scanner.nextLine().trim();
        int interviewId;
        try {
            interviewId = Integer.parseInt(interviewIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Interview ID. Please enter a valid number.");
            return;
        }

        Interview interview = findInterviewById(interviewId);
        if (interview == null) {
            System.out.println("Interview not found.");
            return;
        }

        System.out.println();
        interview.displayDetails();
    }

    private static void searchInterviewsByCandidate(Scanner scanner) {
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
        if (candidate == null) {
            System.out.println("Candidate not found.");
            return;
        }

        ArrayList<Interview> results = new ArrayList<>();
        for (Interview interview : interviews) {
            Application application = findApplicationById(interview.getApplicationId());
            if (application != null && application.getCandidateId() == candidateId) {
                results.add(interview);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No interviews found for this candidate.");
            return;
        }

        System.out.println("\n===== INTERVIEW SEARCH RESULTS =====");
        for (Interview interview : results) {
            System.out.println();
            interview.displayDetails();
        }
    }

    private static void searchInterviewsByJob(Scanner scanner) {
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
        if (job == null) {
            System.out.println("Job not found.");
            return;
        }

        ArrayList<Interview> results = new ArrayList<>();
        for (Interview interview : interviews) {
            Application application = findApplicationById(interview.getApplicationId());
            if (application != null && application.getJobId() == jobId) {
                results.add(interview);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No interviews found for this job.");
            return;
        }

        System.out.println("\n===== INTERVIEW SEARCH RESULTS =====");
        for (Interview interview : results) {
            System.out.println();
            interview.displayDetails();
        }
    }

    private static void updateInterviewStatus(Scanner scanner) {
        System.out.print("Enter Interview ID: ");
        String interviewIdStr = scanner.nextLine().trim();
        int interviewId;
        try {
            interviewId = Integer.parseInt(interviewIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Interview ID. Please enter a valid number.");
            return;
        }

        Interview interview = findInterviewById(interviewId);
        if (interview == null) {
            System.out.println("Interview not found.");
            return;
        }

        System.out.println("\n===== UPDATE INTERVIEW STATUS =====");
        System.out.println("Current Status: " + interview.getStatus());
        System.out.println("1. Scheduled");
        System.out.println("2. Completed");
        System.out.println("3. Cancelled");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();

        String newStatus;
        switch (choice) {
            case "1":
                newStatus = "Scheduled";
                break;
            case "2":
                newStatus = "Completed";
                break;
            case "3":
                newStatus = "Cancelled";
                break;
            default:
                System.out.println("Invalid status choice.");
                return;
        }

        interview.setStatus(newStatus);
        System.out.println("Interview status updated successfully.");
    }

    private static void recordInterviewResult(Scanner scanner) {
        System.out.print("Enter Interview ID: ");
        String interviewIdStr = scanner.nextLine().trim();
        int interviewId;
        try {
            interviewId = Integer.parseInt(interviewIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Interview ID. Please enter a valid number.");
            return;
        }

        Interview interview = findInterviewById(interviewId);
        if (interview == null) {
            System.out.println("Interview not found.");
            return;
        }

        if (!interview.getStatus().equals("Completed")) {
            System.out.println("Interview must be completed before recording the result.");
            return;
        }

        System.out.println("\n===== RECORD INTERVIEW RESULT =====");
        System.out.println("1. Passed");
        System.out.println("2. Failed");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();

        String result;
        switch (choice) {
            case "1":
                result = "Passed";
                break;
            case "2":
                result = "Failed";
                break;
            default:
                System.out.println("Invalid result choice.");
                return;
        }

        interview.setResult(result);
        System.out.println("Interview result recorded successfully.");
    }

    private static Interview findInterviewById(int interviewId) {
        for (Interview interview : interviews) {
            if (interview.getInterviewId() == interviewId) {
                return interview;
            }
        }
        return null;
    }

    private static Company findCompanyById(int companyId) {
        for (Company company : companies) {
            if (company.getCompanyId() == companyId) {
                return company;
            }
        }
        return null;
    }

    private static Company findCompanyByName(String companyName) {
        for (Company company : companies) {
            if (company.getCompanyName().equalsIgnoreCase(companyName.trim())) {
                return company;
            }
        }
        return null;
    }

    private static Recruiter findRecruiterById(int recruiterId) {
        for (Recruiter recruiter : recruiters) {
            if (recruiter.getRecruiterId() == recruiterId) {
                return recruiter;
            }
        }
        return null;
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
