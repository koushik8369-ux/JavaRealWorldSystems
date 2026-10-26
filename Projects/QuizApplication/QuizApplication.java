import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * QuizApplication
 * 
 * A console-based Multiple-Choice Quiz Application built with core Java.
 * Features:
 * - 10 core questions covering Java Fundamentals, OOP, Arrays, Strings, and Exceptions.
 * - Input validation with case-insensitive response handling (A, B, C, D).
 * - Instant feedback showing whether each answer was correct or incorrect.
 * - Score report including correct answers, incorrect answers, and percentage.
 * - Performance evaluation message based on final score.
 * - Replay option to allow multiple attempts.
 */
public class QuizApplication {

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
     * Manages quiz questions, execution flow, scoring, and performance summary.
     */
    static class Quiz {
        private final List<Question> questions;
        private int correctCount;
        private int incorrectCount;

        public Quiz() {
            this.questions = new ArrayList<>();
            loadQuestions();
        }

        /**
         * Initializes 10 multiple-choice questions covering core Java topics.
         */
        private void loadQuestions() {
            // Topic 1: Java Fundamentals
            questions.add(new Question(
                "What is the default value of a boolean primitive variable in Java?",
                new String[]{"true", "false", "null", "0"},
                'B'
            ));

            questions.add(new Question(
                "Which memory area in the JVM is primarily used to store objects created using the 'new' keyword?",
                new String[]{"Stack Memory", "Method Area", "Heap Memory", "Program Counter Register"},
                'C'
            ));

            // Topic 2: Object-Oriented Programming (OOP)
            questions.add(new Question(
                "Which OOP principle is implemented by making fields private and providing public getters and setters?",
                new String[]{"Inheritance", "Polymorphism", "Abstraction", "Encapsulation"},
                'D'
            ));

            questions.add(new Question(
                "Which keyword is used in Java to prevent a class from being subclassed (inherited)?",
                new String[]{"static", "final", "abstract", "super"},
                'B'
            ));

            // Topic 3: Arrays
            questions.add(new Question(
                "What runtime exception is thrown if you try to access an array with an index equal to its length?",
                new String[]{"NullPointerException", "IllegalArgumentException", "ArrayIndexOutOfBoundsException", "ArrayStoreException"},
                'C'
            ));

            questions.add(new Question(
                "Which of the following is used to find the number of elements in a Java array?",
                new String[]{"array.length()", "array.length", "array.size()", "array.capacity()"},
                'B'
            ));

            // Topic 4: Strings
            questions.add(new Question(
                "Why are String objects in Java considered immutable?",
                new String[]{
                    "Once created, their character sequence cannot be changed",
                    "They cannot be passed into any methods",
                    "They can only store primitive numerical characters",
                    "Their reference variables cannot be reassigned"
                },
                'A'
            ));

            questions.add(new Question(
                "Which method should be used to compare the actual character contents of two String objects for equality?",
                new String[]{"==", "equals()", "compareToExact()", "isEqual()"},
                'B'
            ));

            // Topic 5: Exception Handling
            questions.add(new Question(
                "Which block in a try-catch structure ALWAYS executes, regardless of whether an exception occurs?",
                new String[]{"try", "catch", "finally", "throw"},
                'C'
            ));

            questions.add(new Question(
                "Which of the following is an unchecked (Runtime) exception in Java?",
                new String[]{"IOException", "SQLException", "ClassNotFoundException", "NullPointerException"},
                'D'
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
            System.out.println("              QUIZ IN PROGRESS                   ");
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
         * Calculates and displays score, percentage, counts, and performance message.
         */
        private void displayResults() {
            int totalQuestions = questions.size();
            double percentage = ((double) correctCount / totalQuestions) * 100.0;

            System.out.println();
            System.out.println("=================================================");
            System.out.println("                 FINAL RESULTS                   ");
            System.out.println("=================================================");
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
                return "Outstanding! You have mastered core Java concepts!";
            } else if (percentage >= 70.0) {
                return "Great job! You have a solid understanding of Java.";
            } else if (percentage >= 50.0) {
                return "Good effort! A little more practice and you will be in great shape.";
            } else {
                return "Keep practicing! Review Java fundamentals, OOP, and exceptions.";
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
        System.out.println("1. The quiz contains 10 multiple-choice questions.");
        System.out.println("2. Topics include: Fundamentals, OOP, Arrays,");
        System.out.println("   Strings, and Exception Handling.");
        System.out.println("3. Each question has four options: A, B, C, D.");
        System.out.println("4. Answers are case-insensitive ('a' or 'A').");
        System.out.println("5. Invalid inputs will be safely rejected.");
        System.out.println("6. You will receive immediate feedback after each");
        System.out.println("   question, followed by a final score report.");
        System.out.println("=================================================");
        System.out.println();
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
                Quiz quiz = new Quiz();
                quiz.start(scanner);
                playAgain = promptPlayAgain(scanner);
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
