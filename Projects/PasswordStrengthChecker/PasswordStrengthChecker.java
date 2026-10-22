import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class PasswordStrengthChecker {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+?";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private static final Set<String> COMMON_PASSWORDS = Set.of(
        "password", "password123", "123456", "123456789",
        "qwerty", "admin", "letmein", "welcome",
        "iloveyou", "abc123", "passw0rd", "12345678"
    );

    // Store only analysis results, never the actual passwords.
    private static final List<String> HISTORY = new ArrayList<>();

    private static String lastReport = "No report generated yet.";

    private static boolean containsUpper(String s) {
        return s.matches(".*[A-Z].*");
    }

    private static boolean containsLower(String s) {
        return s.matches(".*[a-z].*");
    }

    private static boolean containsDigit(String s) {
        return s.matches(".*[0-9].*");
    }

    private static boolean containsSymbol(String s) {
        return s.matches(".*[^a-zA-Z0-9].*");
    }

    private static boolean hasRepeatedCharacters(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSequentialPattern(String s) {
        String lower = s.toLowerCase();

        String[] patterns = {
            "1234", "2345", "3456", "4567", "5678",
            "6789", "abcd", "bcde", "cdef", "qwerty"
        };

        for (String pattern : patterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    private static int calculateScore(String password) {
        int score = 0;

        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 15;
        if (password.length() >= 16) score += 10;
        if (containsUpper(password)) score += 15;
        if (containsLower(password)) score += 10;
        if (containsDigit(password)) score += 15;
        if (containsSymbol(password)) score += 15;

        if (hasRepeatedCharacters(password)) score -= 10;
        if (hasSequentialPattern(password)) score -= 15;
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            score = 0;
        }

        return Math.max(0, Math.min(100, score));
    }

    private static String getStrength(String password, int score) {
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            return "WEAK - Common password";
        }
        if (score < 40) return "WEAK";
        if (score < 70) return "MEDIUM";
        return "STRONG";
    }

    private static List<String> getSuggestions(String password) {
        List<String> suggestions = new ArrayList<>();

        if (password.length() < 12) {
            suggestions.add("Use at least 12 characters.");
        }
        if (!containsUpper(password)) {
            suggestions.add("Add uppercase letters.");
        }
        if (!containsLower(password)) {
            suggestions.add("Add lowercase letters.");
        }
        if (!containsDigit(password)) {
            suggestions.add("Add numbers.");
        }
        if (!containsSymbol(password)) {
            suggestions.add("Add special characters.");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            suggestions.add("Avoid common passwords; choose a unique passphrase.");
        }
        if (hasRepeatedCharacters(password)) {
            suggestions.add("Avoid repeated adjacent characters.");
        }
        if (hasSequentialPattern(password)) {
            suggestions.add("Avoid predictable sequences.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("All basic checks passed. This score is only an estimate.");
        }

        return suggestions;
    }

    private static void checkPassword() {
        System.out.print("Enter password to check: ");
        String password = SCANNER.nextLine();

        if (password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        int score = calculateScore(password);
        String strength = getStrength(password, score);
        List<String> suggestions = getSuggestions(password);

        StringBuilder report = new StringBuilder();
        report.append("PASSWORD SECURITY REPORT\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("--------------------------------\n");
        report.append("Score: ").append(score).append("/100\n");
        report.append("Strength: ").append(strength).append("\n");
        report.append("Length: ").append(password.length()).append(" characters\n");
        report.append("Uppercase: ").append(containsUpper(password) ? "Yes" : "No").append("\n");
        report.append("Lowercase: ").append(containsLower(password) ? "Yes" : "No").append("\n");
        report.append("Number: ").append(containsDigit(password) ? "Yes" : "No").append("\n");
        report.append("Special character: ").append(containsSymbol(password) ? "Yes" : "No").append("\n");
        report.append("Common password match: ")
              .append(COMMON_PASSWORDS.contains(password.toLowerCase()) ? "Yes" : "No")
              .append("\n");
        report.append("\nSuggestions:\n");

        for (String suggestion : suggestions) {
            report.append("- ").append(suggestion).append("\n");
        }

        report.append("\nNote: This is a basic educational estimate, not a security guarantee.\n");
        report.append("The password itself is not included in this report.\n");

        lastReport = report.toString();

        // Keep only non-sensitive summaries in session history.
        String historyEntry = "Score: " + score + "/100 | " + strength;
        HISTORY.add(historyEntry);

        System.out.println("\n" + lastReport);
    }

    private static void showHistory() {
        System.out.println("\n--- Session History ---");

        if (HISTORY.isEmpty()) {
            System.out.println("No passwords checked in this session.");
            return;
        }

        for (int i = 0; i < HISTORY.size(); i++) {
            System.out.println((i + 1) + ". " + HISTORY.get(i));
        }

        System.out.println("Actual passwords are never stored in history.");
    }

    private static void exportReport() {
        if (lastReport.equals("No report generated yet.")) {
            System.out.println("Check a password first to create a report.");
            return;
        }

        String filename = "PasswordSecurityReport.txt";

        try {
            Files.writeString(Path.of(filename), lastReport);
            System.out.println("Report saved to: " +
                Path.of(filename).toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Could not save report: " + e.getMessage());
        }
    }

    private static void generatePassword() {
        System.out.print("Password length (12-64): ");
        String input = SCANNER.nextLine();

        int length;

        try {
            length = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (length < 12 || length > 64) {
            System.out.println("Length must be between 12 and 64.");
            return;
        }

        StringBuilder password = new StringBuilder();

        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        while (password.length() < length) {
            password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        for (int i = password.length() - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(j));
            password.setCharAt(j, temp);
        }

        System.out.println("Generated password: " + password);
        System.out.println("Store it securely; don't share it.");
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n================================");
            System.out.println(" PASSWORD STRENGTH CHECKER V4");
            System.out.println("================================");
            System.out.println("1. Check a password");
            System.out.println("2. Generate a password");
            System.out.println("3. View session history");
            System.out.println("4. Export last security report");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = SCANNER.nextLine();

            switch (choice) {
                case "1":
                    checkPassword();
                    break;
                case "2":
                    generatePassword();
                    break;
                case "3":
                    showHistory();
                    break;
                case "4":
                    exportReport();
                    break;
                case "5":
                    System.out.println("Thanks for using Password Checker V4!");
                    SCANNER.close();
                    return;
                default:
                    System.out.println("Invalid choice. Select 1-5.");
            }
        }
    }
}