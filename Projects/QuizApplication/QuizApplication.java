import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * QuizApplication
 * 
 * A console-based Multiple-Choice Quiz Application built with core Java.
 * Features:
 * - Player profile support with custom player names.
 * - Topic selection: Java Fundamentals, OOP, Arrays, Strings, Collections, Exceptions, or All Topics.
 * - Three distinct difficulty levels: Easy, Medium, and Hard.
 * - Configurable question timer (defaulting to 20 seconds).
 * - Automatic timeout detection marking unanswered questions as incorrect.
 * - Robust input validation and case-insensitive answering (A, B, C, D).
 * - Instant feedback showing whether each answer was correct, incorrect, or timed out.
 * - Persistent storage of quiz attempts in a local CSV file (quiz_results.csv).
 * - Leaderboard displaying the top 10 attempts ranked by percentage score.
 * - Safe handling of special characters (commas, quotes) in player names.
 */
public class QuizApplication {

    private static final String CSV_FILE_NAME = "quiz_results.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Configurable time limit in seconds per question (defaults to 20 seconds)
    private static int defaultTimeLimitSeconds = 20;

    // Asynchronous console input queue managed by a single background daemon thread
    private static final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private static final String EOF_MARKER = "\u0000__EOF__\u0000";

    static {
        Thread consoleReader = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    inputQueue.put(line);
                }
                inputQueue.put(EOF_MARKER);
            } catch (IOException | InterruptedException ignored) {
                try {
                    inputQueue.put(EOF_MARKER);
                } catch (InterruptedException ignored2) {
                }
            }
        }, "ConsoleInputReaderThread");
        consoleReader.setDaemon(true);
        consoleReader.start();
    }

    /**
     * Reads a single line of input from the console queue indefinitely.
     * Returns null if the input stream terminates (EOF).
     */
    private static String readLine() {
        try {
            String line = inputQueue.take();
            if (EOF_MARKER.equals(line)) {
                return null;
            }
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

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
     * Enum representing the supported quiz topics.
     */
    enum Topic {
        ALL("All Topics"),
        FUNDAMENTALS("Java Fundamentals"),
        OOP("Object-Oriented Programming"),
        ARRAYS("Arrays"),
        STRINGS("Strings"),
        COLLECTIONS("Collections Framework"),
        EXCEPTIONS("Exception Handling");

        private final String displayName;

        Topic(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
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
        private final String topic;
        private final int correctAnswers;
        private final int totalQuestions;
        private final double percentage;
        private final String timestamp;

        public QuizResult(String playerName, String difficulty, int correctAnswers,
                          int totalQuestions, double percentage, String timestamp) {
            this(playerName, extractDifficulty(difficulty), extractTopic(difficulty), correctAnswers,
                 totalQuestions, percentage, timestamp);
        }

        public QuizResult(String playerName, String difficulty, String topic, int correctAnswers,
                          int totalQuestions, double percentage, String timestamp) {
            this.playerName = playerName;
            this.difficulty = difficulty;
            this.topic = topic;
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

        public String getTopic() {
            return topic;
        }

        public String getDifficultyAndTopic() {
            return "Not recorded".equals(topic) ? difficulty : difficulty + " (" + topic + ")";
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

        private static String extractTopic(String difficulty) {
            if (difficulty == null) {
                return "Not recorded";
            }
            int topicStart = difficulty.lastIndexOf('(');
            if (topicStart < 0 || !difficulty.endsWith(")")) {
                return "Not recorded";
            }
            String topicName = difficulty.substring(topicStart + 1, difficulty.length() - 1).trim();
            for (Topic topic : Topic.values()) {
                if (topic.getDisplayName().equalsIgnoreCase(topicName)) {
                    return topic.getDisplayName();
                }
            }
            return "Not recorded";
        }

        private static String extractDifficulty(String difficulty) {
            String topic = extractTopic(difficulty);
            if ("Not recorded".equals(topic)) {
                return difficulty;
            }
            return difficulty.substring(0, difficulty.lastIndexOf('(')).trim();
        }
    }

    /**
     * Inner class representing a single multiple-choice question.
     */
    static class Question {
        private final Topic topic;
        private final String questionText;
        private final String[] options;
        private final char correctOption; // 'A', 'B', 'C', or 'D'

        public Question(Topic topic, String questionText, String[] options, char correctOption) {
            this.topic = topic;
            this.questionText = questionText;
            this.options = options;
            this.correctOption = Character.toUpperCase(correctOption);
        }

        public Topic getTopic() {
            return topic;
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
         * Displays the question, its options, and the clear time limit.
         */
        public void displayQuestion(int questionNumber, int timeLimitSeconds) {
            System.out.println("Question " + questionNumber + " [" + topic.getDisplayName() + "]: " + questionText);
            char label = 'A';
            for (String option : options) {
                System.out.println("  " + label + ") " + option);
                label++;
            }
            System.out.println("[Time limit: " + timeLimitSeconds + " seconds]");
        }
    }

    /**
     * Manages quiz questions for the selected difficulty level and topic,
     * execution flow, scoring, timer management, and performance summary.
     */
    static class Quiz {
        private final String playerName;
        private final Difficulty difficulty;
        private final Topic topic;
        private final int timeLimitSeconds;
        private final List<Question> questions;
        private int correctCount;
        private int incorrectCount;

        public Quiz(String playerName, Difficulty difficulty, Topic topic, int timeLimitSeconds) {
            this.playerName = playerName;
            this.difficulty = difficulty;
            this.topic = topic;
            this.timeLimitSeconds = timeLimitSeconds;
            this.questions = new ArrayList<>();
            loadQuestionsForDifficultyAndTopic(difficulty, topic);
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        public Topic getTopic() {
            return topic;
        }

        public int getTimeLimitSeconds() {
            return timeLimitSeconds;
        }

        /**
         * Populates the question list according to the chosen difficulty and topic.
         */
        private void loadQuestionsForDifficultyAndTopic(Difficulty difficulty, Topic topic) {
            List<Question> bank = new ArrayList<>();
            switch (difficulty) {
                case EASY -> loadEasyQuestions(bank);
                case MEDIUM -> loadMediumQuestions(bank);
                case HARD -> loadHardQuestions(bank);
            }

            for (Question q : bank) {
                if (topic == Topic.ALL || q.getTopic() == topic) {
                    questions.add(q);
                }
            }
        }

        /**
         * Easy level questions covering core concepts across topics.
         */
        private void loadEasyQuestions(List<Question> bank) {
            // Fundamentals
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "What is the size of an int data type in Java?",
                new String[]{"8 bits", "16 bits", "32 bits", "64 bits"},
                'C'
            ));
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "Which keyword is used to declare a constant variable in Java whose value cannot be reassigned?",
                new String[]{"static", "const", "final", "var"},
                'C'
            ));

            // OOP
            bank.add(new Question(
                Topic.OOP,
                "Which keyword is used by a class to inherit another class in Java?",
                new String[]{"implements", "extends", "inherits", "super"},
                'B'
            ));
            bank.add(new Question(
                Topic.OOP,
                "Which OOP concept combines fields and methods into a single unit while restricting direct outside access?",
                new String[]{"Polymorphism", "Inheritance", "Encapsulation", "Abstraction"},
                'C'
            ));

            // Arrays
            bank.add(new Question(
                Topic.ARRAYS,
                "What is the index of the very first element in any Java array?",
                new String[]{"0", "1", "-1", "null"},
                'A'
            ));
            bank.add(new Question(
                Topic.ARRAYS,
                "Which property is used to determine the total number of elements in a Java array?",
                new String[]{"length()", "length", "size()", "capacity"},
                'B'
            ));

            // Strings
            bank.add(new Question(
                Topic.STRINGS,
                "Which method is used to find the total number of characters in a String?",
                new String[]{"size()", "length()", "capacity()", "count()"},
                'B'
            ));
            bank.add(new Question(
                Topic.STRINGS,
                "Which method extracts a single character from a String at a given index?",
                new String[]{"charAt()", "getChar()", "charOf()", "indexOf()"},
                'A'
            ));

            // Collections
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which List class in the Java Collections Framework provides a resizable array?",
                new String[]{"ArrayList", "HashSet", "HashMap", "TreeMap"},
                'A'
            ));
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which method is used to append an element to an ArrayList in Java?",
                new String[]{"push()", "put()", "add()", "insert()"},
                'C'
            ));

            // Exceptions
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "Which keyword is used in Java to explicitly throw an exception object?",
                new String[]{"throws", "catch", "throw", "try"},
                'C'
            ));
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "Which block in Java is used to enclose code that might throw an exception?",
                new String[]{"catch", "try", "finally", "throw"},
                'B'
            ));
        }

        /**
         * Medium level questions covering intermediate Java topics.
         */
        private void loadMediumQuestions(List<Question> bank) {
            // Fundamentals
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "What is the default value of an uninitialized instance variable of reference type in Java?",
                new String[]{"0", "\"\"", "null", "undefined"},
                'C'
            ));
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "Which primitive type is used to store single-precision 32-bit floating-point numbers in Java?",
                new String[]{"double", "float", "long", "decimal"},
                'B'
            ));

            // OOP
            bank.add(new Question(
                Topic.OOP,
                "Which special method is invoked when an object is created and has the same name as its class?",
                new String[]{"Method", "Constructor", "Destructor", "Initializer"},
                'B'
            ));
            bank.add(new Question(
                Topic.OOP,
                "Which keyword is used within a subclass method to invoke a superclass constructor or method?",
                new String[]{"this", "parent", "base", "super"},
                'D'
            ));

            // Arrays
            bank.add(new Question(
                Topic.ARRAYS,
                "What runtime exception occurs if you access an array with an index equal to or greater than its length?",
                new String[]{"ArrayIndexOutOfBoundsException", "NullPointerException", "IllegalArgumentException", "ArrayStoreException"},
                'A'
            ));
            bank.add(new Question(
                Topic.ARRAYS,
                "What is the default initial value of elements inside a newly allocated int array (new int[5])?",
                new String[]{"null", "0", "-1", "undefined"},
                'B'
            ));

            // Strings
            bank.add(new Question(
                Topic.STRINGS,
                "Why are String objects in Java considered immutable?",
                new String[]{
                    "They can only store numeric values",
                    "Once created, their character sequence cannot be changed",
                    "They cannot be passed into methods as arguments",
                    "Their reference variables can never be reassigned"
                },
                'B'
            ));
            bank.add(new Question(
                Topic.STRINGS,
                "Which class provides a mutable character sequence and is optimized for non-thread-safe string concatenations?",
                new String[]{"String", "StringBuffer", "StringBuilder", "CharSequence"},
                'C'
            ));

            // Collections
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which Collection interface implementation does NOT allow duplicate elements and does not guarantee insertion order?",
                new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"},
                'C'
            ));
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which Map implementation maintains its key-value pairs in their order of insertion?",
                new String[]{"HashMap", "LinkedHashMap", "TreeMap", "Hashtable"},
                'B'
            ));

            // Exceptions
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "Which block in exception handling ALWAYS executes whether an exception was thrown or not?",
                new String[]{"try", "catch", "finally", "throw"},
                'C'
            ));
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "Which keyword is placed in a method signature to declare exceptions that might be thrown out of the method?",
                new String[]{"throw", "throws", "try", "catch"},
                'B'
            ));
        }

        /**
         * Hard level questions covering advanced Java concepts.
         */
        private void loadHardQuestions(List<Question> bank) {
            // Fundamentals / Memory
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "In which JVM memory area are string literals stored to optimize memory reuse?",
                new String[]{"Call Stack", "String Constant Pool", "Native Method Stack", "Register Area"},
                'B'
            ));
            bank.add(new Question(
                Topic.FUNDAMENTALS,
                "What memory guarantee does the 'volatile' keyword provide to a variable in multithreaded Java?",
                new String[]{
                    "Guarantees atomicity of compound operations like incrementing",
                    "Ensures memory visibility across threads and prevents CPU instruction reordering",
                    "Prevents the variable from being garbage collected",
                    "Synchronizes access across all methods using object monitors"
                },
                'B'
            ));

            // OOP
            bank.add(new Question(
                Topic.OOP,
                "What is the key difference between method overloading and method overriding?",
                new String[]{
                    "Overloading occurs at runtime; overriding occurs at compile time",
                    "Overloading happens in the same class with different parameters; overriding happens in subclasses with the exact same signature",
                    "Overloading requires class inheritance; overriding does not",
                    "Overloading modifies access privileges only; overriding changes method names"
                },
                'B'
            ));
            bank.add(new Question(
                Topic.OOP,
                "How does Java eliminate the classic multiple-inheritance Diamond Problem with classes?",
                new String[]{
                    "By allowing a class to extend only one single direct superclass",
                    "By using runtime dynamic method dispatch over interfaces",
                    "By requiring all methods in parent classes to be declared final",
                    "By forbidding default methods in interfaces"
                },
                'A'
            ));

            // Arrays / Generics
            bank.add(new Question(
                Topic.ARRAYS,
                "Why cannot you directly instantiate a generic array like 'new T[10]' in Java?",
                new String[]{
                    "Due to type erasure at compile time where type parameters are erased at runtime",
                    "Because arrays are not allowed to hold references to objects",
                    "Because generics only work with primitive types",
                    "Because arrays are dynamically resized at runtime"
                },
                'A'
            ));
            bank.add(new Question(
                Topic.ARRAYS,
                "Which sorting algorithm is primarily implemented by Arrays.sort() for primitive arrays in modern Java?",
                new String[]{"Dual-Pivot Quicksort", "Bubble Sort", "Insertion Sort", "Heap Sort"},
                'A'
            ));

            // Strings / Concurrency
            bank.add(new Question(
                Topic.STRINGS,
                "Which class should be preferred when performing heavy string concatenations across multiple concurrent threads?",
                new String[]{"StringBuilder", "StringBuffer", "String", "CharSequence"},
                'B'
            ));
            bank.add(new Question(
                Topic.STRINGS,
                "What does invoking the intern() method on a String object return in Java?",
                new String[]{
                    "A new heap copy of the string",
                    "The canonical instance from the String Constant Pool",
                    "A char array representation of the string",
                    "An encrypted hash of the string"
                },
                'B'
            ));

            // Collections
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which Map implementation stores key-value pairs sorted according to the natural ordering of keys or a custom Comparator?",
                new String[]{"HashMap", "LinkedHashMap", "TreeMap", "Hashtable"},
                'C'
            ));
            bank.add(new Question(
                Topic.COLLECTIONS,
                "Which Queue implementation orders elements according to their natural ordering or a specified Comparator via a priority heap?",
                new String[]{"ArrayDeque", "PriorityQueue", "LinkedList", "ConcurrentLinkedQueue"},
                'B'
            ));

            // Exceptions
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "What distinguishes checked exceptions from unchecked exceptions in Java?",
                new String[]{
                    "Unchecked exceptions inherit from Exception; checked inherit from Error",
                    "Checked exceptions are verified at compile-time and must be handled or declared; unchecked exceptions inherit from RuntimeException",
                    "Checked exceptions cannot be caught in a catch block",
                    "Unchecked exceptions must always be declared using throws"
                },
                'B'
            ));
            bank.add(new Question(
                Topic.EXCEPTIONS,
                "Which interface must a resource class implement to be safely used inside a Java try-with-resources statement?",
                new String[]{"Closeable only", "AutoCloseable", "Serializable", "Disposable"},
                'B'
            ));
        }

        /**
         * Runs the quiz from start to finish with question timers.
         * 
         * @return The QuizResult object representing this completed attempt.
         */
        public QuizResult start() {
            correctCount = 0;
            incorrectCount = 0;

            if (questions.isEmpty()) {
                System.out.println("No questions are available for " + topic.getDisplayName()
                    + " at the " + difficulty.getDisplayName()
                    + " level. Please choose another topic or difficulty.");
                return null;
            }

            System.out.println("=================================================");
            System.out.println("     QUIZ IN PROGRESS [" + difficulty.getDisplayName().toUpperCase() + " LEVEL]");
            System.out.println("     Topic : " + topic.getDisplayName());
            System.out.println("     Player: " + playerName);
            System.out.println("     Timer : " + timeLimitSeconds + "s per question");
            System.out.println("=================================================");

            int totalQuestions = questions.size();
            for (int i = 0; i < totalQuestions; i++) {
                Question question = questions.get(i);
                System.out.println();
                question.displayQuestion(i + 1, timeLimitSeconds);

                Character userChoice = promptForTimedOption(timeLimitSeconds);

                if (userChoice == null) {
                    System.out.println("\n>> Time's up! You exceeded the " + timeLimitSeconds + "-second time limit.");
                    System.out.println("   The correct answer was: " 
                        + question.getCorrectOption() + ") " + question.getCorrectOptionText());
                    incorrectCount++;
                } else {
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
            }

            double percentage = ((double) correctCount / totalQuestions) * 100.0;
            displayResults(totalQuestions, percentage);

            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);

            return new QuizResult(playerName, difficulty.getDisplayName(),
                                  topic.getDisplayName(), correctCount,
                                  totalQuestions, percentage, timestamp);
        }

        /**
         * Prompts the user with a strict time limit.
         * Returns the chosen option (A, B, C, or D) or null if the timer expires.
         */
        private Character promptForTimedOption(int timeLimitSeconds) {
            long deadline = System.currentTimeMillis() + (timeLimitSeconds * 1000L);
            System.out.print("Your answer (A, B, C, D): ");

            while (true) {
                long remainingMs = deadline - System.currentTimeMillis();
                if (remainingMs <= 0) {
                    return null; // Time expired
                }

                long remainingSec = (remainingMs + 999) / 1000;
                String line;
                try {
                    line = inputQueue.poll(remainingSec, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                if (line == null || EOF_MARKER.equals(line)) {
                    return null; // Time expired or EOF reached
                }

                String trimmed = line.trim();
                if (trimmed.length() == 1) {
                    char choice = Character.toUpperCase(trimmed.charAt(0));
                    if (choice >= 'A' && choice <= 'D') {
                        return choice;
                    }
                }

                long afterRemainingMs = deadline - System.currentTimeMillis();
                if (afterRemainingMs <= 0) {
                    return null; // Time expired during retry
                }
                long afterRemainingSec = (afterRemainingMs + 999) / 1000;
                System.out.println("Invalid input! Please enter only A, B, C, or D (" + afterRemainingSec + "s remaining).");
                System.out.print("Your answer (A, B, C, D): ");
            }
        }

        /**
         * Calculates and displays score, percentage, counts, selected difficulty,
         * selected topic, player name, and performance message.
         */
        private void displayResults(int totalQuestions, double percentage) {
            System.out.println();
            System.out.println("=================================================");
            System.out.println("                 FINAL RESULTS                   ");
            System.out.println("=================================================");
            System.out.println("Player Name      : " + playerName);
            System.out.println("Topic            : " + topic.getDisplayName());
            System.out.println("Difficulty Level : " + difficulty.getDisplayName());
            System.out.println("Time Limit       : " + timeLimitSeconds + "s per question");
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

    private static boolean hasValidCsvQuoting(String line) {
        boolean inQuotes = false;
        boolean atFieldStart = true;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    i++;
                } else if (inQuotes) {
                    inQuotes = false;
                } else if (atFieldStart) {
                    inQuotes = true;
                } else {
                    return false;
                }
            } else if (current == ',' && !inQuotes) {
                atFieldStart = true;
            } else if (!inQuotes) {
                atFieldStart = false;
            }
        }
        return !inQuotes;
    }

    private static String normalizeHeader(String header) {
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    /**
     * Appends a completed quiz attempt to the persistent CSV results file.
     * Preserves standard 6-column schema for complete backward compatibility.
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
                escapeCsvField(result.getDifficultyAndTopic()),
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
     * Supports standard 6-column format as well as optional 7-column format seamlessly.
     */
    private static List<QuizResult> loadResults() {
        List<QuizResult> results = new ArrayList<>();
        File file = getResultsFile();

        if (!file.exists() || file.length() == 0) {
            return results;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null || !hasValidCsvQuoting(headerLine)) {
                return results;
            }
            List<String> headers = parseCsvLine(headerLine);
            int nameIndex = -1;
            int difficultyIndex = -1;
            int topicIndex = -1;
            int correctIndex = -1;
            int totalIndex = -1;
            int percentageIndex = -1;
            int timestampIndex = -1;
            for (int i = 0; i < headers.size(); i++) {
                switch (normalizeHeader(headers.get(i))) {
                    case "playername" -> nameIndex = i;
                    case "difficulty" -> difficultyIndex = i;
                    case "topic" -> topicIndex = i;
                    case "correctanswers" -> correctIndex = i;
                    case "totalquestions" -> totalIndex = i;
                    case "percentage" -> percentageIndex = i;
                    case "datetime", "timestamp" -> timestampIndex = i;
                    default -> {
                    }
                }
            }
            if (nameIndex < 0 || difficultyIndex < 0 || correctIndex < 0
                    || totalIndex < 0 || percentageIndex < 0 || timestampIndex < 0) {
                System.err.println("Notice: Quiz results file has an unsupported header.");
                return results;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !hasValidCsvQuoting(line)) {
                    continue;
                }
                List<String> tokens = parseCsvLine(line);
                int requiredIndex = Math.max(nameIndex,
                    Math.max(difficultyIndex, Math.max(correctIndex,
                    Math.max(totalIndex, Math.max(percentageIndex, timestampIndex)))));
                if (tokens.size() <= requiredIndex || (topicIndex >= 0 && tokens.size() <= topicIndex)) {
                    continue;
                }
                try {
                    String name = tokens.get(nameIndex);
                    String difficulty = tokens.get(difficultyIndex);
                    String encodedTopic = QuizResult.extractTopic(difficulty);
                    String topic = topicIndex >= 0 && !tokens.get(topicIndex).isEmpty()
                        ? tokens.get(topicIndex) : encodedTopic;
                    int correct = Integer.parseInt(tokens.get(correctIndex));
                    int total = Integer.parseInt(tokens.get(totalIndex));
                    double percentage = Double.parseDouble(tokens.get(percentageIndex));
                    String timestamp = tokens.get(timestampIndex);

                    if (name.isEmpty() || difficulty.isEmpty() || total <= 0
                            || correct < 0 || correct > total || !Double.isFinite(percentage)
                            || percentage < 0.0 || percentage > 100.0 || timestamp.isEmpty()) {
                        continue;
                    }
                    String difficultyLevel = difficulty;
                    if (!"Not recorded".equals(encodedTopic)) {
                        int topicStart = difficulty.lastIndexOf('(');
                        difficultyLevel = difficulty.substring(0, topicStart).trim();
                    }
                    results.add(new QuizResult(name, difficultyLevel, topic, correct, total,
                                               percentage, timestamp));
                } catch (NumberFormatException ignored) {
                    // Skip malformed numeric records while retaining valid attempts.
                }
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not read leaderboard file: " + e.getMessage());
        }

        return results;
    }

    private static List<QuizResult> findPlayerResults(String playerName) {
        List<QuizResult> matches = new ArrayList<>();
        for (QuizResult result : loadResults()) {
            if (result.getPlayerName().equalsIgnoreCase(playerName.trim())) {
                matches.add(result);
            }
        }
        return matches;
    }

    private static void displayQuizHistory(String playerName) {
        List<QuizResult> results = findPlayerResults(playerName);
        System.out.println();
        System.out.println("QUIZ HISTORY FOR " + (results.isEmpty()
            ? playerName : results.get(0).getPlayerName()));
        if (results.isEmpty()) {
            System.out.println("No saved quiz attempts found for this player.");
            return;
        }
        System.out.printf("%-18s %-14s %-32s %-28s %-10s %-8s %-12s%n",
            "Player", "Date/Time", "Topic", "Difficulty", "Correct", "Total", "Percentage");
        for (QuizResult result : results) {
            System.out.printf("%-18s %-14s %-32s %-28s %-10d %-8d %.1f%%%n",
                result.getPlayerName(), result.getTimestamp(), result.getTopic(), result.getDifficulty(),
                result.getCorrectAnswers(), result.getTotalQuestions(), result.getPercentage());
        }
    }

    private static void displayPlayerStatistics(String playerName) {
        List<QuizResult> results = findPlayerResults(playerName);
        System.out.println();
        System.out.println("PLAYER STATISTICS FOR " + playerName);
        if (results.isEmpty()) {
            System.out.println("No saved quiz attempts found for this player.");
            return;
        }

        double percentageTotal = 0.0;
        double highestPercentage = 0.0;
        long questionsAnswered = 0;
        long correctAnswers = 0;
        for (QuizResult result : results) {
            percentageTotal += result.getPercentage();
            highestPercentage = Math.max(highestPercentage, result.getPercentage());
            questionsAnswered += result.getTotalQuestions();
            correctAnswers += result.getCorrectAnswers();
        }
        double averagePercentage = percentageTotal / results.size();
        double accuracy = questionsAnswered == 0
            ? 0.0 : ((double) correctAnswers / questionsAnswered) * 100.0;

        System.out.println("Player Name              : " + results.get(0).getPlayerName());
        System.out.println("Total Attempts            : " + results.size());
        System.out.printf(Locale.US, "Average Percentage        : %.1f%%%n", averagePercentage);
        System.out.printf(Locale.US, "Highest Percentage        : %.1f%%%n", highestPercentage);
        System.out.println("Total Questions Answered  : " + questionsAnswered);
        System.out.println("Total Correct Answers     : " + correctAnswers);
        System.out.printf(Locale.US, "Accuracy Percentage       : %.1f%%%n", accuracy);
    }

    /**
     * Displays the top 10 leaderboard scores ranked by percentage (highest first).
     */
    private static void displayLeaderboard() {
        List<QuizResult> results = loadResults();

        System.out.println();
        System.out.println("===================================================================================================");
        System.out.println("                                    QUIZ LEADERBOARD (TOP 10)                                      ");
        System.out.println("===================================================================================================");

        if (results.isEmpty()) {
            System.out.println("  No quiz records found yet! Take a quiz to become the first on the leaderboard!");
            System.out.println("===================================================================================================");
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

        System.out.printf("%-5s %-22s %-25s %-10s %-12s %-20s%n",
            "Rank", "Player Name", "Difficulty/Topic", "Score", "Percentage", "Date/Time");
        System.out.println("---------------------------------------------------------------------------------------------------");

        int limit = Math.min(10, results.size());
        for (int i = 0; i < limit; i++) {
            QuizResult r = results.get(i);
            String displayName = r.getPlayerName();
            if (displayName.length() > 20) {
                displayName = displayName.substring(0, 17) + "...";
            }
            String diffTopic = r.getDifficultyAndTopic();
            if (diffTopic.length() > 24) {
                diffTopic = diffTopic.substring(0, 21) + "...";
            }
            String scoreStr = r.getCorrectAnswers() + " / " + r.getTotalQuestions();
            String pctStr = String.format(Locale.US, "%.1f%%", r.getPercentage());

            System.out.printf("%-5d %-22s %-25s %-10s %-12s %-20s%n",
                (i + 1), displayName, diffTopic, scoreStr, pctStr, r.getTimestamp());
        }
        System.out.println("===================================================================================================");
    }

    /**
     * Prompts the player to enter a valid non-empty name.
     */
    private static String promptPlayerName() {
        while (true) {
            System.out.print("Enter your name: ");
            String name = readLine();
            if (name == null) {
                return "Player";
            }
            name = name.trim();
            if (!name.isEmpty()) {
                return name;
            }
            System.out.println("Player name cannot be empty. Please enter a valid name.");
        }
    }

    /**
     * Prompts the player to choose a quiz topic or select All Topics.
     */
    private static Topic selectTopic() {
        System.out.println("Select Quiz Topic:");
        System.out.println("  1) All Topics");
        System.out.println("  2) Java Fundamentals");
        System.out.println("  3) Object-Oriented Programming");
        System.out.println("  4) Arrays");
        System.out.println("  5) Strings");
        System.out.println("  6) Collections Framework");
        System.out.println("  7) Exception Handling");

        while (true) {
            System.out.print("Enter your choice (1-7 or topic name): ");
            String input = readLine();
            if (input == null) {
                return Topic.ALL;
            }
            String choice = input.trim().toUpperCase(Locale.ROOT);
            switch (choice) {
                case "1":
                case "ALL":
                case "ALL TOPICS":
                    return Topic.ALL;
                case "2":
                case "FUNDAMENTALS":
                case "JAVA FUNDAMENTALS":
                    return Topic.FUNDAMENTALS;
                case "3":
                case "OOP":
                case "OBJECT-ORIENTED PROGRAMMING":
                case "OBJECT ORIENTED PROGRAMMING":
                case "OBJECT-ORIENTED PROGRAMMING (OOP)":
                    return Topic.OOP;
                case "4":
                case "ARRAYS":
                    return Topic.ARRAYS;
                case "5":
                case "STRINGS":
                    return Topic.STRINGS;
                case "6":
                case "COLLECTIONS":
                case "COLLECTIONS FRAMEWORK":
                    return Topic.COLLECTIONS;
                case "7":
                case "EXCEPTIONS":
                case "EXCEPTION HANDLING":
                    return Topic.EXCEPTIONS;
                default:
                    System.out.println("Invalid choice! Please enter a number from 1 to 7 or the topic name.");
            }
        }
    }

    /**
     * Prompts the user to select a difficulty level with validation.
     */
    private static Difficulty selectDifficulty() {
        System.out.println("Select Difficulty Level:");
        System.out.println("  1) Easy");
        System.out.println("  2) Medium");
        System.out.println("  3) Hard");

        while (true) {
            System.out.print("Enter your choice (1-3 or Easy/Medium/Hard): ");
            String input = readLine();
            if (input == null) {
                return Difficulty.EASY;
            }
            input = input.trim();
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
     * Displays the welcome banner and quiz instructions.
     */
    private static void displayWelcomeAndInstructions() {
        System.out.println("=================================================");
        System.out.println("         WELCOME TO JAVA QUIZ APPLICATION        ");
        System.out.println("=================================================");
        System.out.println("Instructions:");
        System.out.println("1. Choose a topic: Fundamentals, OOP, Arrays,");
        System.out.println("   Strings, Collections, Exceptions, or All Topics.");
        System.out.println("2. Choose a difficulty level: Easy, Medium, or Hard.");
        System.out.println("3. Each question has four options: A, B, C, D.");
        System.out.println("4. Answers are case-insensitive ('a' or 'A').");
        System.out.println("5. Each question has a time limit (default: " + defaultTimeLimitSeconds + "s,");
        System.out.println("   configurable in Settings).");
        System.out.println("6. If time expires, the question is marked wrong");
        System.out.println("   and the correct answer is revealed.");
        System.out.println("7. Invalid inputs will be safely rejected.");
        System.out.println("8. Immediate feedback is given after each answer.");
        System.out.println("9. Scores are permanently saved to the Leaderboard!");
        System.out.println("=================================================");
        System.out.println();
    }

    /**
     * Allows the user to configure the question time limit in seconds.
     */
    private static void configureSettings() {
        System.out.println();
        System.out.println("---------------- SETTINGS ----------------");
        System.out.println("Current Question Time Limit: " + defaultTimeLimitSeconds + " seconds");
        System.out.print("Enter new time limit in seconds (1-120, or press Enter to keep current): ");
        String input = readLine();
        if (input == null || input.trim().isEmpty()) {
            System.out.println("No changes made. Keeping " + defaultTimeLimitSeconds + " seconds.");
            return;
        }

        try {
            int newLimit = Integer.parseInt(input.trim());
            if (newLimit >= 1 && newLimit <= 120) {
                defaultTimeLimitSeconds = newLimit;
                System.out.println(">> Time limit successfully updated to " + defaultTimeLimitSeconds + " seconds per question.");
            } else {
                System.out.println("Invalid value! Please enter a number between 1 and 120 seconds.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }

    /**
     * Prompts the user whether they would like to attempt another quiz.
     */
    private static boolean promptPlayAgain() {
        while (true) {
            System.out.print("\nWould you like to try another quiz? (Y/N): ");
            String input = readLine();
            if (input == null) {
                return false;
            }
            input = input.trim();
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
        System.out.println("3) Quiz History");
        System.out.println("4) Player Statistics");
        System.out.println("5) Instructions");
        System.out.println("6) Settings (Timer: " + defaultTimeLimitSeconds + "s)");
        System.out.println("7) Exit");
        System.out.println("-------------------------------------------");
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("         WELCOME TO JAVA QUIZ APPLICATION        ");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            displayMainMenu();
            System.out.print("Select an option (1-7): ");
            String choice = readLine();
            if (choice == null) {
                break;
            }
            choice = choice.trim();

            switch (choice) {
                case "1" -> {
                    System.out.println();
                    String playerName = promptPlayerName();
                    boolean playAgain = true;
                    while (playAgain) {
                        System.out.println();
                        Topic selectedTopic = selectTopic();
                        System.out.println();
                        Difficulty selectedDifficulty = selectDifficulty();
                        System.out.println();

                        Quiz quiz = new Quiz(playerName, selectedDifficulty, selectedTopic, defaultTimeLimitSeconds);
                        QuizResult result = quiz.start();
                        if (result != null) {
                            saveResult(result);
                        }

                        playAgain = promptPlayAgain();
                    }
                }
                case "2" -> displayLeaderboard();
                case "3" -> displayQuizHistory(promptPlayerName());
                case "4" -> displayPlayerStatistics(promptPlayerName());
                case "5" -> displayWelcomeAndInstructions();
                case "6" -> configureSettings();
                case "7" -> {
                    running = false;
                    System.out.println();
                    System.out.println("Thank you for taking the Java Quiz. Keep learning and coding!");
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid selection! Please enter a number between 1 and 7.");
            }
        }
    }
}
