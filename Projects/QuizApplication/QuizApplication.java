import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * QuizApplication
 * 
 * A console-based Multiple-Choice Quiz Application built with core Java.
 * Features:
 * - Player profile support with custom player names.
 * - Three distinct difficulty levels: Easy, Medium, and Hard.
 * - Question sets covering Java Fundamentals, OOP, Arrays, Strings, Collections, and Exceptions.
 * - Robust input validation and case-insensitive answering (A, B, C, D).
 * - Instant feedback showing whether each answer was correct or incorrect.
 * - Persistent storage of quiz attempts in a local CSV file (quiz_results.csv).
 * - Leaderboard displaying the top 10 attempts ranked by percentage score.
 * - Safe handling of special characters (commas, quotes) in player names.
 */
public class QuizApplication {

    private static final String CSV_FILE_NAME = "quiz_results.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Resolves the target results CSV file.
     * Guarantees the file is placed inside Projects/QuizApplication whether run
     * from the repository root or from within the project directory.
     */
    private static File getResultsFile() {
        File subDirFile = new File("Projects" + File.separator + "QuizApplication", CSV_FILE_NAME);
        if (subDirFile.getParentFile() != null && subDirFile.getParentFile().isDirectory()) {
            return subDirFile;
        }
        return new File(CSV_FILE_NAME);
    }

    /**
     * Enum representing the supported quiz difficulty levels.
     */
    enum Difficulty {
        EASY("Easy"),
        MEDIUM("Medium"),
        HARD("Hard");

        private final String displayName;

        Difficulty(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents a single historical quiz result for persistence and leaderboard display.
     */
    static class QuizResult {
        private final String playerName;
        private final String difficulty;
        private final int correctAnswers;
        private final int totalQuestions;
        private final double percentage;
        private final String timestamp;

        public QuizResult(String playerName, String difficulty, int correctAnswers,
                          int totalQuestions, double percentage, String timestamp) {
            this.playerName = playerName;
            this.difficulty = difficulty;
            this.correctAnswers = correctAnswers;
            this.totalQuestions = totalQuestions;
            this.percentage = percentage;
            this.timestamp = timestamp;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public double getPercentage() {
            return percentage;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Inner class representing a single multiple-choice question.
     */
    static class Question {
        private final String questionText;
        private final String[] options;
        private final char correctOption; // 'A', 'B', 'C', or 'D'

        public Question(String questionText, String[] options, char correctOption) {
            this.questionText = questionText;
            this.options = options;
            this.correctOption = Character.toUpperCase(correctOption);
        }

        public String getQuestionText() {
            return questionText;
        }

        public String[] getOptions() {
            return options;
        }

        public char getCorrectOption() {
            return correctOption;
        }

        public String getCorrectOptionText() {
            int index = correctOption - 'A';
            if (index >= 0 && index < options.length) {
                return options[index];
            }
            return "";
        }

        public boolean isCorrect(char selectedOption) {
            return Character.toUpperCase(selectedOption) == correctOption;
        }

        /**
         * Displays the question and its four options formatted neatly.
         */
        public void displayQuestion(int questionNumber) {
            System.out.println("Question " + questionNumber + ": " + questionText);
            char label = 'A';
            for (String option : options) {
                System.out.println("  " + label + ") " + option);
                label++;
            }
        }
    }

    /**
     * Manages quiz questions for the selected difficulty level, execution flow,
     * scoring, and performance summary.
     */
    static class Quiz {
        private final String playerName;
        private final Difficulty difficulty;
        private final List<Question> questions;
        private int correctCount;
        private int incorrectCount;

        public Quiz(String playerName, Difficulty difficulty) {
            this.playerName = playerName;
            this.difficulty = difficulty;
            this.questions = new ArrayList<>();
            loadQuestionsForDifficulty(difficulty);
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        /**
         * Populates the question list according to the chosen difficulty level.
         */
        private void loadQuestionsForDifficulty(Difficulty difficulty) {
            switch (difficulty) {
                case EASY -> loadEasyQuestions();
                case MEDIUM -> loadMediumQuestions();
                case HARD -> loadHardQuestions();
            }
        }

        /**
         * Easy level questions covering core concepts.
         */
        private void loadEasyQuestions() {
            // Fundamentals
            questions.add(new Question(
                "What is the size of an int data type in Java?",
                new String[]{"8 bits", "16 bits", "32 bits", "64 bits"},
                'C'
            ));

            // OOP
            questions.add(new Question(
                "Which keyword is used by a class to inherit another class in Java?",
                new String[]{"implements", "extends", "inherits", "super"},
                'B'
            ));

            // Arrays
            questions.add(new Question(
                "What is the index of the very first element in any Java array?",
                new String[]{"0", "1", "-1", "null"},
                'A'
            ));

            // Strings
            questions.add(new Question(
                "Which method is used to find the total number of characters in a String?",
                new String[]{"size()", "length()", "capacity()", "count()"},
                'B'
            ));

            // Collections
            questions.add(new Question(
                "Which List class in the Java Collections Framework provides a resizable array?",
                new String[]{"ArrayList", "HashSet", "HashMap", "TreeMap"},
                'A'
            ));

            // Exceptions
            questions.add(new Question(
                "Which keyword is used in Java to explicitly throw an exception object?",
                new String[]{"throws", "catch", "throw", "try"},
                'C'
            ));
        }

        /**
         * Medium level questions covering intermediate Java topics.
         */
        private void loadMediumQuestions() {
            // Fundamentals
            questions.add(new Question(
                "What is the default value of an uninitialized instance variable of reference type in Java?",
                new String[]{"0", "\"\"", "null", "undefined"},
                'C'
            ));

            // OOP
            questions.add(new Question(
                "Which special method is invoked when an object is created and has the same name as its class?",
                new String[]{"Method", "Constructor", "Destructor", "Initializer"},
                'B'
            ));

            // Arrays
            questions.add(new Question(
                "What runtime exception occurs if you access an array with an index equal to or greater than its length?",
                new String[]{"ArrayIndexOutOfBoundsException", "NullPointerException", "IllegalArgumentException", "ArrayStoreException"},
                'A'
            ));

            // Strings
            questions.add(new Question(
                "Why are String objects in Java considered immutable?",
                new String[]{
                    "They can only store numeric values",
                    "Once created, their character sequence cannot be changed",
                    "They cannot be passed into methods as arguments",
                    "Their reference variables can never be reassigned"
                },
                'B'
            ));

            // Collections
            questions.add(new Question(
                "Which Collection interface implementation does NOT allow duplicate elements and does not guarantee insertion order?",
                new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"},
                'C'
            ));

            // Exceptions
            questions.add(new Question(
                "Which block in exception handling ALWAYS executes whether an exception was thrown or not?",
                new String[]{"try", "catch", "finally", "throw"},
                'C'
            ));
        }

        /**
         * Hard level questions covering advanced Java concepts.
         */
        private void loadHardQuestions() {
            // Fundamentals / Memory
            questions.add(new Question(
                "In which JVM memory area are string literals stored to optimize memory reuse?",
                new String[]{"Call Stack", "String Constant Pool", "Native Method Stack", "Register Area"},
                'B'
            ));

            // OOP
            questions.add(new Question(
                "What is the key difference between method overloading and method overriding?",
                new String[]{
                    "Overloading occurs at runtime; overriding occurs at compile time",
                    "Overloading happens in the same class with different parameters; overriding happens in subclasses with the exact same signature",
                    "Overloading requires class inheritance; overriding does not",
                    "Overloading modifies access privileges only; overriding changes method names"
                },
                'B'
            ));

            // Arrays / Generics
            questions.add(new Question(
                "Why cannot you directly instantiate a generic array like 'new T[10]' in Java?",
                new String[]{
                    "Due to type erasure at compile time where type parameters are erased at runtime",
                    "Because arrays are not allowed to hold references to objects",
                    "Because generics only work with primitive types",
                    "Because arrays are dynamically resized at runtime"
                },
                'A'
            ));

            // Strings / Concurrency
            questions.add(new Question(
                "Which class should be preferred when performing heavy string concatenations across multiple concurrent threads?",
                new String[]{"StringBuilder", "StringBuffer", "String", "CharSequence"},
                'B'
            ));

            // Collections
            questions.add(new Question(
                "Which Map implementation stores key-value pairs sorted according to the natural ordering of keys or a custom Comparator?",
                new String[]{"HashMap", "LinkedHashMap", "TreeMap", "Hashtable"},
                'C'
            ));

            // Exceptions
            questions.add(new Question(
                "What distinguishes checked exceptions from unchecked exceptions in Java?",
                new String[]{
                    "Unchecked exceptions inherit from Exception; checked inherit from Error",
                    "Checked exceptions are verified at compile-time and must be handled or declared; unchecked exceptions inherit from RuntimeException",
                    "Checked exceptions cannot be caught in a catch block",
                    "Unchecked exceptions must always be declared using throws"
                },
                'B'
            ));
        }

        /**
         * Runs the quiz from start to finish.
         * 
         * @param scanner Shared Scanner instance for console reading.
         * @return The QuizResult object representing this completed attempt.
         */
        public QuizResult start(Scanner scanner) {
            correctCount = 0;
            incorrectCount = 0;

            System.out.println("=================================================");
            System.out.println("     QUIZ IN PROGRESS [" + difficulty.getDisplayName().toUpperCase() + " LEVEL]");
            System.out.println("     Player: " + playerName);
            System.out.println("=================================================");

            int totalQuestions = questions.size();
            for (int i = 0; i < totalQuestions; i++) {
                Question question = questions.get(i);
                System.out.println();
                question.displayQuestion(i + 1);

                char userChoice = promptForValidOption(scanner);

                if (question.isCorrect(userChoice)) {
                    System.out.println(">> Correct! Well done.");
                    correctCount++;
                } else {
                    System.out.println(">> Incorrect!");
                    System.out.println("   The correct answer was: " 
                        + question.getCorrectOption() + ") " + question.getCorrectOptionText());
                    incorrectCount++;
                }
            }

            double percentage = ((double) correctCount / totalQuestions) * 100.0;
            displayResults(totalQuestions, percentage);

            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            return new QuizResult(playerName, difficulty.getDisplayName(), correctCount,
                                  totalQuestions, percentage, timestamp);
        }

        /**
         * Continuously prompts the user until a valid option (A, B, C, or D) is entered.
         */
        private char promptForValidOption(Scanner scanner) {
            while (true) {
                System.out.print("Your answer (A, B, C, D): ");
                String input = scanner.nextLine().trim();

                if (input.length() == 1) {
                    char choice = Character.toUpperCase(input.charAt(0));
                    if (choice >= 'A' && choice <= 'D') {
                        return choice;
                    }
                }
                System.out.println("Invalid input! Please enter only A, B, C, or D.");
            }
        }

        /**
         * Calculates and displays score, percentage, counts, selected difficulty,
         * player name, and performance message.
         */
        private void displayResults(int totalQuestions, double percentage) {
            System.out.println();
            System.out.println("=================================================");
            System.out.println("                 FINAL RESULTS                   ");
            System.out.println("=================================================");
            System.out.println("Player Name      : " + playerName);
            System.out.println("Difficulty Level : " + difficulty.getDisplayName());
            System.out.println("Total Questions  : " + totalQuestions);
            System.out.println("Correct Answers  : " + correctCount);
            System.out.println("Incorrect Answers: " + incorrectCount);
            System.out.printf("Final Score      : %d / %d%n", correctCount, totalQuestions);
            System.out.printf("Percentage       : %.1f%%%n", percentage);
            System.out.println("-------------------------------------------------");
            System.out.println("Performance      : " + getPerformanceFeedback(percentage));
            System.out.println("=================================================");
        }

        /**
         * Returns an encouraging evaluation message based on the percentage score.
         */
        private String getPerformanceFeedback(double percentage) {
            if (percentage >= 90.0) {
                return "Outstanding! You have mastered these Java concepts!";
            } else if (percentage >= 70.0) {
                return "Great job! You have a solid understanding of Java.";
            } else if (percentage >= 50.0) {
                return "Good effort! A little more practice and you will be in great shape.";
            } else {
                return "Keep practicing! Review Java fundamentals, OOP, collections, and exceptions.";
            }
        }
    }

    /**
     * Escapes a string to conform with CSV standards (RFC 4180).
     * If the field contains commas, double quotes, or newlines, it wraps the field
     * in double quotes and escapes any internal quotes as double-double-quotes.
     */
    public static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Parses a single line from a CSV file respecting quoted fields and escaped quotes.
     */
    public static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++; // Skip the second quote in ""
                    } else {
                        inQuotes = false; // Closing quote
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true; // Opening quote
                } else if (c == ',') {
                    values.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        values.add(sb.toString().trim());
        return values;
    }

    /**
     * Appends a completed quiz attempt to the persistent CSV results file.
     */
    private static void saveResult(QuizResult result) {
        File file = getResultsFile();
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.println("Player Name,Difficulty,Correct Answers,Total Questions,Percentage,Date/Time");
            }
            writer.printf(Locale.US, "%s,%s,%d,%d,%.1f,%s%n",
                escapeCsvField(result.getPlayerName()),
                escapeCsvField(result.getDifficulty()),
                result.getCorrectAnswers(),
                result.getTotalQuestions(),
                result.getPercentage(),
                escapeCsvField(result.getTimestamp())
            );
            System.out.println(">> Result successfully saved to the leaderboard!");
        } catch (IOException e) {
            System.err.println("Warning: Unable to save quiz result: " + e.getMessage());
        }
    }

    /**
     * Reads all quiz attempts from the CSV file.
     */
    private static List<QuizResult> loadResults() {
        List<QuizResult> results = new ArrayList<>();
        File file = getResultsFile();

        if (!file.exists() || file.length() == 0) {
            return results;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header line
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 6) {
                    try {
                        String name = tokens.get(0);
                        String difficulty = tokens.get(1);
                        int correct = Integer.parseInt(tokens.get(2));
                        int total = Integer.parseInt(tokens.get(3));
                        double percentage = Double.parseDouble(tokens.get(4));
                        String timestamp = tokens.get(5);

                        results.add(new QuizResult(name, difficulty, correct, total, percentage, timestamp));
                    } catch (NumberFormatException ignored) {
                        // Skip malformed records safely
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not read leaderboard file: " + e.getMessage());
        }

        return results;
    }

    /**
     * Displays the top 10 leaderboard scores ranked by percentage (highest first).
     */
    private static void displayLeaderboard() {
        List<QuizResult> results = loadResults();

        System.out.println();
        System.out.println("=========================================================================================");
        System.out.println("                              QUIZ LEADERBOARD (TOP 10)                                 ");
        System.out.println("=========================================================================================");

        if (results.isEmpty()) {
            System.out.println("  No quiz records found yet! Take a quiz to become the first on the leaderboard!");
            System.out.println("=========================================================================================");
            return;
        }

        // Sort by percentage descending; tie-break by correct answers descending
        results.sort((r1, r2) -> {
            int cmp = Double.compare(r2.getPercentage(), r1.getPercentage());
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(r2.getCorrectAnswers(), r1.getCorrectAnswers());
        });

        System.out.printf("%-5s %-25s %-12s %-10s %-12s %-20s%n",
            "Rank", "Player Name", "Difficulty", "Score", "Percentage", "Date/Time");
        System.out.println("-----------------------------------------------------------------------------------------");

        int limit = Math.min(10, results.size());
        for (int i = 0; i < limit; i++) {
            QuizResult r = results.get(i);
            String displayName = r.getPlayerName();
            if (displayName.length() > 23) {
                displayName = displayName.substring(0, 20) + "...";
            }
            String scoreStr = r.getCorrectAnswers() + " / " + r.getTotalQuestions();
            String pctStr = String.format(Locale.US, "%.1f%%", r.getPercentage());

            System.out.printf("%-5d %-25s %-12s %-10s %-12s %-20s%n",
                (i + 1), displayName, r.getDifficulty(), scoreStr, pctStr, r.getTimestamp());
        }
        System.out.println("=========================================================================================");
    }

    /**
     * Prompts the player to enter a valid non-empty name.
     */
    private static String promptPlayerName(Scanner scanner) {
        while (true) {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                return name;
            }
            System.out.println("Player name cannot be empty. Please enter a valid name.");
        }
    }

    /**
     * Displays the welcome banner and quiz instructions.
     */
    private static void displayWelcomeAndInstructions() {
        System.out.println("=================================================");
        System.out.println("         WELCOME TO JAVA QUIZ APPLICATION        ");
        System.out.println("=================================================");
        System.out.println("Instructions:");
        System.out.println("1. Choose a difficulty level: Easy, Medium, or Hard.");
        System.out.println("2. Topics include: Fundamentals, OOP, Arrays,");
        System.out.println("   Strings, Collections, and Exception Handling.");
        System.out.println("3. Each question has four options: A, B, C, D.");
        System.out.println("4. Answers are case-insensitive ('a' or 'A').");
        System.out.println("5. Invalid inputs will be safely rejected.");
        System.out.println("6. Immediate feedback is given after each answer.");
        System.out.println("7. Scores are permanently saved to the Leaderboard!");
        System.out.println("=================================================");
        System.out.println();
    }

    /**
     * Prompts the user to select a difficulty level with validation.
     */
    private static Difficulty selectDifficulty(Scanner scanner) {
        System.out.println("Select Difficulty Level:");
        System.out.println("  1) Easy");
        System.out.println("  2) Medium");
        System.out.println("  3) Hard");

        while (true) {
            System.out.print("Enter your choice (1-3 or Easy/Medium/Hard): ");
            String input = scanner.nextLine().trim();

            if (input.equals("1") || input.equalsIgnoreCase("EASY")) {
                return Difficulty.EASY;
            } else if (input.equals("2") || input.equalsIgnoreCase("MEDIUM")) {
                return Difficulty.MEDIUM;
            } else if (input.equals("3") || input.equalsIgnoreCase("HARD")) {
                return Difficulty.HARD;
            } else {
                System.out.println("Invalid choice! Please enter 1, 2, 3, or the difficulty name.");
            }
        }
    }

    /**
     * Prompts the user whether they would like to attempt another quiz.
     */
    private static boolean promptPlayAgain(Scanner scanner) {
        while (true) {
            System.out.print("\nWould you like to try another quiz? (Y/N): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("YES")) {
                return true;
            } else if (input.equalsIgnoreCase("N") || input.equalsIgnoreCase("NO")) {
                return false;
            } else {
                System.out.println("Invalid choice! Please enter 'Y' for Yes or 'N' for No.");
            }
        }
    }

    /**
     * Displays the primary application menu.
     */
    private static void displayMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1) Start Quiz");
        System.out.println("2) View Leaderboard");
        System.out.println("3) Instructions");
        System.out.println("4) Exit");
        System.out.println("-------------------------------------------");
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=================================================");
            System.out.println("         WELCOME TO JAVA QUIZ APPLICATION        ");
            System.out.println("=================================================");

            boolean running = true;
            while (running) {
                displayMainMenu();
                System.out.print("Select an option (1-4): ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        System.out.println();
                        String playerName = promptPlayerName(scanner);
                        boolean playAgain = true;
                        while (playAgain) {
                            System.out.println();
                            Difficulty selectedDifficulty = selectDifficulty(scanner);
                            System.out.println();

                            Quiz quiz = new Quiz(playerName, selectedDifficulty);
                            QuizResult result = quiz.start(scanner);
                            saveResult(result);

                            playAgain = promptPlayAgain(scanner);
                        }
                    }
                    case "2" -> displayLeaderboard();
                    case "3" -> displayWelcomeAndInstructions();
                    case "4" -> {
                        running = false;
                        System.out.println();
                        System.out.println("Thank you for taking the Java Quiz. Keep learning and coding!");
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid selection! Please enter a number between 1 and 4.");
                }
            }
        } finally {
            // Safely close the scanner to release standard input resources
            scanner.close();
        }
    }
}
