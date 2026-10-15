import java.security.SecureRandom;
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

    private static String analyzePassword(String password) {
        String normalized = password.toLowerCase();

        if (COMMON_PASSWORDS.contains(normalized)) {
            return "WEAK - Common password";
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (containsUpper(password)) score++;
        if (containsLower(password)) score++;
        if (containsDigit(password)) score++;
        if (containsSymbol(password)) score++;

        if (hasRepeatedCharacters(password)) score--;
        if (hasSequentialPattern(password)) score--;

        if (score <= 2) return "WEAK";
        if (score <= 4) return "MEDIUM";
        return "STRONG";
    }

    private static void checkPassword() {
        System.out.print("Enter password to check: ");
        String password = SCANNER.nextLine();

        if (password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.println("\n--- Password Report ---");
        System.out.println("Length 8+: " +
            (password.length() >= 8 ? "PASS" : "FAIL"));
        System.out.println("Uppercase: " +
            (containsUpper(password) ? "PASS" : "FAIL"));
        System.out.println("Lowercase: " +
            (containsLower(password) ? "PASS" : "FAIL"));
        System.out.println("Number: " +
            (containsDigit(password) ? "PASS" : "FAIL"));
        System.out.println("Special character: " +
            (containsSymbol(password) ? "PASS" : "FAIL"));
        System.out.println("Repeated adjacent characters: " +
            (hasRepeatedCharacters(password) ? "DETECTED" : "NOT DETECTED"));
        System.out.println("Sequential pattern: " +
            (hasSequentialPattern(password) ? "DETECTED" : "NOT DETECTED"));
        System.out.println("Strength: " + analyzePassword(password));

        System.out.println("\nSuggestions:");

        boolean found = false;

        if (password.length() < 12) {
            System.out.println("- Use at least 12 characters.");
            found = true;
        }
        if (!containsUpper(password)) {
            System.out.println("- Add uppercase letters.");
            found = true;
        }
        if (!containsLower(password)) {
            System.out.println("- Add lowercase letters.");
            found = true;
        }
        if (!containsDigit(password)) {
            System.out.println("- Add numbers.");
            found = true;
        }
        if (!containsSymbol(password)) {
            System.out.println("- Add special characters.");
            found = true;
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            System.out.println("- Avoid common passwords.");
            found = true;
        }
        if (hasRepeatedCharacters(password)) {
            System.out.println("- Avoid repeated adjacent characters.");
            found = true;
        }
        if (hasSequentialPattern(password)) {
            System.out.println("- Avoid predictable sequences.");
            found = true;
        }

        if (!found) {
            System.out.println("- All basic checks passed.");
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
            System.out.println(" PASSWORD STRENGTH CHECKER V3");
            System.out.println("================================");
            System.out.println("1. Check a password");
            System.out.println("2. Generate a password");
            System.out.println("3. Exit");
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
                    System.out.println("Thanks for using Password Checker V3!");
                    SCANNER.close();
                    return;
                default:
                    System.out.println("Invalid choice. Select 1, 2, or 3.");
            }
        }
    }
}