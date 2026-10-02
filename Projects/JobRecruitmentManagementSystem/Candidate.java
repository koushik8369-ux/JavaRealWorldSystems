public class Candidate {
    private int candidateId;
    private String name;
    private int age;
    private String email;
    private String phone;
    private String skills;

    public Candidate(int candidateId, String name, int age, String email, String phone, String skills) {
        this.candidateId = candidateId;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.skills = skills;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void displayDetails() {
        System.out.println("Candidate ID : " + candidateId);
        System.out.println("Name         : " + name);
        System.out.println("Age          : " + age);
        System.out.println("Email        : " + email);
        System.out.println("Phone        : " + phone);
        System.out.println("Skills       : " + skills);
    }
}
