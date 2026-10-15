import java.util.Scanner;

public class PasswordStrengthChecker {

    public static String checkStrength(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        if (score <= 2) {
            return "WEAK";
        } else if (score <= 4) {
            return "MEDIUM";
        } else {
            return "STRONG";
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== PASSWORD STRENGTH CHECKER ===");
        System.out.print("Enter your password: ");

        String password = scanner.nextLine();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            scanner.close();
            return;
        }

        System.out.println("\nPassword Analysis:");
        System.out.println("Length (8+): "
                + (password.length() >= 8 ? "PASS" : "FAIL"));
        System.out.println("Uppercase: "
                + (password.matches(".*[A-Z].*") ? "PASS" : "FAIL"));
        System.out.println("Lowercase: "
                + (password.matches(".*[a-z].*") ? "PASS" : "FAIL"));
        System.out.println("Number: "
                + (password.matches(".*[0-9].*") ? "PASS" : "FAIL"));
        System.out.println("Special character: "
                + (password.matches(".*[^a-zA-Z0-9].*") ? "PASS" : "FAIL"));

        System.out.println("\nPassword Strength: "
                + checkStrength(password));

        scanner.close();
    }
}