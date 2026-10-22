public class PasswordStrengthCheckerTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void test(String name, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + name);
            passed++;
        } else {
            System.out.println("[FAIL] " + name);
            failed++;
        }
    }

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" PASSWORD STRENGTH CHECKER V5 TESTS");
        System.out.println("======================================\n");

        // Common password detection
        test(
            "Detect common password",
            PasswordStrengthChecker.isCommonPassword("password123")
        );

        test(
            "Common password check ignores case",
            PasswordStrengthChecker.isCommonPassword("PASSWORD123")
        );

        test(
            "Accept password absent from common list",
            !PasswordStrengthChecker.isCommonPassword("River!Cloud82Star")
        );

        // Character checks
        test(
            "Detect uppercase letters",
            PasswordStrengthChecker.containsUpper("Hello123!")
        );

      test(
            "Detect lowercase letters",
            PasswordStrengthChecker.containsLower("Hello123!")
        );
        test(
            "Detect digits",
            PasswordStrengthChecker.containsDigit("Hello123!")
        );

        test(
            "Detect special characters",
            PasswordStrengthChecker.containsSymbol("Hello123!")
        );

        // Weak patterns
        test(
            "Detect repeated adjacent characters",
            PasswordStrengthChecker.hasRepeatedCharacters("Hello!!123")
        );

        test(
            "Detect sequential patterns",
            PasswordStrengthChecker.hasSequentialPattern("MyPass1234!")
        );

        test(
            "No repeated adjacent characters",
            !PasswordStrengthChecker.hasRepeatedCharacters("Abc!123X")
        );

        // Scoring
        test(
            "Empty password gets zero score",
            PasswordStrengthChecker.calculateScore("") == 0
        );

        test(
            "Null password gets zero score",
            PasswordStrengthChecker.calculateScore(null) == 0
        );

        test(
            "Common password gets zero score",
            PasswordStrengthChecker.calculateScore("password123") == 0
        );

        test(
            "Score never exceeds 100",
            PasswordStrengthChecker.calculateScore("Strong!River2026Cloud") <= 100
        );

        test(
            "Score is never negative",
            PasswordStrengthChecker.calculateScore("aaaa1234") >= 0
        );

        // Strength classification
        test(
            "Common password classified as weak",
            PasswordStrengthChecker.getStrength("password123")
                .startsWith("WEAK")
        );

        test(
            "Strong example classified correctly",
            PasswordStrengthChecker.getStrength("River!Cloud82Star")
                .equals("STRONG")
        );

        // Suggestions
        test(
            "Weak password receives suggestions",
            !PasswordStrengthChecker.getSuggestions("abc").isEmpty()
        );

        test(
            "Sequential pattern receives suggestion",
            PasswordStrengthChecker.getSuggestions("Abcd1234!XYZ")
                .stream()
                .anyMatch(s -> s.contains("sequences"))
        );

        System.out.println("\n======================================");
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
        System.out.println("======================================");

        if (failed > 0) {
            System.out.println("RESULT: SOME TESTS FAILED");
            System.exit(1);
        } else {
            System.out.println("RESULT: ALL TESTS PASSED");
        }
    }
}