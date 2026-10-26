import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * QuizApplication
 * 
 * A console-based Multiple-Choice Quiz Application built with core Java.
 * Features:
 * - Three distinct difficulty levels: Easy, Medium, and Hard.
 * - Dedicated question sets covering Java Fundamentals, OOP, Arrays, Strings, Collections, and Exceptions.
 * - Input validation with case-insensitive response handling (A, B, C, D).
 * - Instant feedback showing whether each answer was correct or incorrect.
 * - Final score report showing selected difficulty, correct answers, incorrect answers, score, and percentage.
 * - Dynamic performance evaluation message based on final score.
 * - Replay option allowing users to try another round or switch difficulty levels.
 */
public class QuizApplication {

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
        private final Difficulty difficulty;
        private final List<Question> questions;
        private int correctCount;
        private int incorrectCount;

        public Quiz(Difficulty difficulty) {
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
         */
        public void start(Scanner scanner) {
            correctCount = 0;
            incorrectCount = 0;

            System.out.println("=================================================");
            System.out.println("     QUIZ IN PROGRESS [" + difficulty.getDisplayName().toUpperCase() + " LEVEL]");
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

            displayResults();
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
         * and performance message.
         */
        private void displayResults() {
            int totalQuestions = questions.size();
            double percentage = ((double) correctCount / totalQuestions) * 100.0;

            System.out.println();
            System.out.println("=================================================");
            System.out.println("                 FINAL RESULTS                   ");
            System.out.println("=================================================");
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
        System.out.println("6. You will receive immediate feedback after each");
        System.out.println("   question, followed by a final score report.");
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
     * Prompts the user whether they would like to attempt the quiz again.
     */
    private static boolean promptPlayAgain(Scanner scanner) {
        while (true) {
            System.out.print("\nWould you like to try the quiz again? (Y/N): ");
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
     * Application entry point.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            displayWelcomeAndInstructions();

            boolean playAgain = true;
            while (playAgain) {
                Difficulty selectedDifficulty = selectDifficulty(scanner);
                System.out.println();

                Quiz quiz = new Quiz(selectedDifficulty);
                quiz.start(scanner);

                playAgain = promptPlayAgain(scanner);
                if (playAgain) {
                    System.out.println();
                }
            }

            System.out.println();
            System.out.println("Thank you for taking the Java Quiz. Keep learning and coding!");
            System.out.println("Goodbye!");
        } finally {
            // Safely close the scanner to release standard input resources
            scanner.close();
        }
    }
}
