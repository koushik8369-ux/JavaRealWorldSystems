
package com.koushik.waste;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class CollectionTaskPersistenceServiceTest {

    private static int checks = 0;

    private static final CollectionTaskPersistenceService SERVICE =
            new CollectionTaskPersistenceService();

    public static void main(String[] args) throws IOException {
        Path directory = Files.createTempDirectory("task-tests");

        try {
            testSaveAndLoad(directory);
            testAllStatuses(directory);
            testSpecialCharacters(directory);
            testMissingFile(directory);
            testInvalidHeader(directory);
            testInvalidStatus(directory);
            testInvalidAssignment(directory);
            testDuplicateTaskIds(directory);
            testNullArguments();
        } finally {
            deleteDirectory(directory);
        }

        System.out.println(
                "\nAll task persistence tests passed: " + checks);
    }

    private static void testSaveAndLoad(Path directory)
            throws IOException {
        Path file = directory.resolve("tasks.csv");

        List<CollectionTaskPersistenceService.TaskRecord> records =
                List.of(
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T1", "B1", "V1",
                                CollectionTask.Status.IN_PROGRESS),
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T2", "B2", null,
                                CollectionTask.Status.COMPLETED));

        SERVICE.saveTasks(records, file);
        List<CollectionTaskPersistenceService.TaskRecord> loaded =
                SERVICE.loadTasks(file);

        check(loaded.size() == 2, "Save and load multiple task records");
        check(loaded.get(0).taskId().equals("T1"),
                "Preserve task ID");
        check(loaded.get(0).binId().equals("B1"),
                "Preserve bin ID");
        check(loaded.get(0).vehicleId().equals("V1"),
                "Preserve assigned vehicle ID");
        check(loaded.get(1).vehicleId() == null,
                "Preserve absent vehicle assignment");
    }

    private static void testAllStatuses(Path directory)
            throws IOException {
        Path file = directory.resolve("statuses.csv");

        List<CollectionTaskPersistenceService.TaskRecord> records =
                List.of(
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T1", "B1", null,
                                CollectionTask.Status.PENDING),
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T2", "B2", "V2",
                                CollectionTask.Status.IN_PROGRESS),
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T3", "B3", null,
                                CollectionTask.Status.COMPLETED));

        SERVICE.saveTasks(records, file);
        List<CollectionTaskPersistenceService.TaskRecord> loaded =
                SERVICE.loadTasks(file);

        check(loaded.get(0).status() == CollectionTask.Status.PENDING,
                "Preserve PENDING status");
        check(loaded.get(1).status() == CollectionTask.Status.IN_PROGRESS,
                "Preserve IN_PROGRESS status");
        check(loaded.get(2).status() == CollectionTask.Status.COMPLETED,
                "Preserve COMPLETED status");
    }

    private static void testSpecialCharacters(Path directory)
            throws IOException {
        Path file = directory.resolve("special.csv");

        List<CollectionTaskPersistenceService.TaskRecord> records =
                List.of(
                        new CollectionTaskPersistenceService.TaskRecord(
                                "T,1", "B\"1", null,
                                CollectionTask.Status.PENDING));

        SERVICE.saveTasks(records, file);
        var loaded = SERVICE.loadTasks(file);

        check(loaded.get(0).taskId().equals("T,1"),
                "Preserve commas in task ID");
        check(loaded.get(0).binId().equals("B\"1"),
                "Preserve quotes in bin ID");
    }

    private static void testMissingFile(Path directory) {
        expectIOException(
                () -> SERVICE.loadTasks(
                        directory.resolve("missing.csv")),
                "Reject missing task file");
    }

    private static void testInvalidHeader(Path directory)
            throws IOException {
        Path file = directory.resolve("bad-header.csv");

        Files.writeString(
                file,
                "wrong,header\nT1,B1,,PENDING\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadTasks(file),
                "Reject invalid task CSV header");
    }

    private static void testInvalidStatus(Path directory)
            throws IOException {
        Path file = directory.resolve("bad-status.csv");

        Files.writeString(
                file,
                "taskId,binId,vehicleId,status\n"
                        + "T1,B1,,UNKNOWN\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadTasks(file),
                "Reject unknown task status");
    }

    private static void testInvalidAssignment(Path directory) {
        expectIllegalArgument(
                () -> new CollectionTaskPersistenceService.TaskRecord(
                        "T1", "B1", null,
                        CollectionTask.Status.IN_PROGRESS),
                "Require vehicle for in-progress task");

        expectIllegalArgument(
                () -> new CollectionTaskPersistenceService.TaskRecord(
                        "T2", "B1", "V1",
                        CollectionTask.Status.COMPLETED),
                "Reject vehicle assignment on completed task");
    }

    private static void testDuplicateTaskIds(Path directory)
            throws IOException {
        Path file = directory.resolve("duplicates.csv");

        Files.writeString(
                file,
                "taskId,binId,vehicleId,status\n"
                        + "T1,B1,,PENDING\n"
                        + "T1,B2,,COMPLETED\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadTasks(file),
                "Reject duplicate task IDs when loading");

        expectIllegalArgument(
                () -> saveTasks(
                        List.of(
                                new CollectionTaskPersistenceService.TaskRecord(
                                        "T1", "B1", null,
                                        CollectionTask.Status.PENDING),
                                new CollectionTaskPersistenceService.TaskRecord(
                                        "T1", "B2", null,
                                        CollectionTask.Status.COMPLETED)),
                        file),
                "Reject duplicate task IDs when saving");
    }

    private static void testNullArguments() {
        expectIllegalArgument(
                () -> saveTasks(null, Path.of("tasks.csv")),
                "Reject null task list");

        expectIllegalArgument(
                () -> saveTasks(List.of(), null),
                "Reject null save path");

        expectIllegalArgument(
                () -> loadTasks(null),
                "Reject null load path");
    }

    private static void saveTasks(
            List<CollectionTaskPersistenceService.TaskRecord> records,
            Path file) {
        try {
            SERVICE.saveTasks(records, file);
        } catch (IOException exception) {
            throw new AssertionError(
                    "Unexpected save error", exception);
        }
    }

    private static List<CollectionTaskPersistenceService.TaskRecord>
            loadTasks(Path file) {
        try {
            return SERVICE.loadTasks(file);
        } catch (IOException exception) {
            throw new AssertionError(
                    "Unexpected load error", exception);
        }
    }

    private static void expectIOException(
            IoAction action, String description) {
        checks++;

        try {
            action.run();
            throw new AssertionError("FAILED: " + description);
        } catch (IOException expected) {
            System.out.println("PASSED: " + description);
        }
    }

    private static void expectIllegalArgument(
            Runnable action, String description) {
        checks++;

        try {
            action.run();
            throw new AssertionError("FAILED: " + description);
        } catch (IllegalArgumentException expected) {
            System.out.println("PASSED: " + description);
        }
    }

    private static void check(boolean condition, String description) {
        checks++;

        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }

        System.out.println("PASSED: " + description);
    }

    private static void deleteDirectory(Path directory)
            throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(
                    Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }
}
