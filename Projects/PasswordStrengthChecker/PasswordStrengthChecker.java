import java.util.Scanner;
import java.util.Set;

public class PasswordStrengthChecker {

    private static final Set<String> COMMON_PASSWORDS = Set.of(
        "password", "password123", "123456", "123456789",
        "qwerty", "admin", "letmein", "welcome",
        "iloveyou", "abc123", "passw0rd", "12345678"
    );

    public static boolean isCommonPassword(String password) {
        return COMMON_PASSWORDS.contains(
            password.toLowerCase()
        );
    }

    public static int calculateScore(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        return score;
    }

    public static String getStrength(String password) {
        if (isCommonPassword(password)) {
            return "WEAK - Common password";
        }

        int score = calculateScore(password);

        if (score <= 2) return "WEAK";
        if (score <= 4) return "MEDIUM";
        return "STRONG";
    }

    public static void showSuggestions(String password) {
        System.out.println("\n--- Password Suggestions ---");

        boolean suggestionFound = false;

        if (password.length() < 12) {
            System.out.println("- Use at least 12 characters.");
            suggestionFound = true;
        }

        if (!password.matches(".*[A-Z].*")) {
            System.out.println("- Add an uppercase letter.");
            suggestionFound = true;
        }

        if (!password.matches(".*[a-z].*")) {
            System.out.println("- Add a lowercase letter.");
            suggestionFound = true;
        }

        if (!password.matches(".*[0-9].*")) {
            System.out.println("- Add a number.");
            suggestionFound = true;
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            System.out.println("- Add a special character.");
            suggestionFound = true;
        }

        if (isCommonPassword(password)) {
            System.out.println("- Avoid common passwords.");
            System.out.println("- Choose a unique passphrase.");
            suggestionFound = true;
        }

        if (!suggestionFound) {
            System.out.println("Good! All basic checks passed.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("  PASSWORD STRENGTH CHECKER V2");
        System.out.println("==================================");

        System.out.print("Enter a password to check: ");
        String password = scanner.nextLine();

        if (password.isBlank()) {
            System.out.println("Error: Password cannot be empty.");
            scanner.close();
            return;
        }

        System.out.println("\n--- Password Analysis ---");

        if (isCommonPassword(password)) {
            System.out.println("Warning: This is a commonly used password!");
        } else {
            System.out.println("Common password check: Not in our small list.");
        }

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
            + getStrength(password));

        showSuggestions(password);

        System.out.println("\n==================================");
        scanner.close();
    }
}