
package com.koushik.waste;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CollectionTaskRestorationTest {

    private static int checks;

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        testRestoreAllStatuses();
        testRestoreRejectsMissingBinWithoutPartialChanges();
        testRestoreRejectsMissingVehicleWithoutPartialChanges();
        testRestoreRejectsVehicleAssignedTwice();
        testRestoreRejectsExistingTaskId();

        System.out.println();
        System.out.println(
                "All task restoration tests passed: " + checks);
    }

    private static void testRestoreAllStatuses() throws IOException {
        Path file = Files.createTempFile("tasks-restore-", ".csv");

        try {
            WasteManagementService bins = new WasteManagementService();

            WasteBin bin1 = new WasteBin("B1", "North", 100, 90);
            WasteBin bin2 = new WasteBin("B2", "South", 100, 25);
            WasteBin bin3 = new WasteBin("B3", "East", 100, 50);

            bins.registerBin(bin1);
            bins.registerBin(bin2);
            bins.registerBin(bin3);

            CollectionOperationsService operations =
                    new CollectionOperationsService();

            operations.registerVehicle(
                    new CollectionVehicle("V1", "REG-001", 100));
            operations.registerVehicle(
                    new CollectionVehicle("V2", "REG-002", 100));

            CollectionTaskPersistenceService persistence =
                    new CollectionTaskPersistenceService();

            persistence.saveTasks(List.of(
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T1", "B2", null,
                            CollectionTask.Status.PENDING),
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T2", "B1", "V1",
                            CollectionTask.Status.IN_PROGRESS),
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T3", "B3", null,
                            CollectionTask.Status.COMPLETED)
            ), file);

            persistence.restoreTasks(file, bins, operations);

            check(operations.getTaskCount() == 3,
                    "Restore all three tasks");

            check(operations.findTask("T1").getStatus()
                            == CollectionTask.Status.PENDING,
                    "Restore pending status");

            check(operations.findTask("T2").getStatus()
                            == CollectionTask.Status.IN_PROGRESS,
                    "Restore in-progress status");

            check(operations.findTask("T3").getStatus()
                            == CollectionTask.Status.COMPLETED,
                    "Restore completed status");

            check(!operations.isVehicleAvailable("V1"),
                    "Restored in-progress task reserves its vehicle");

            check(operations.isVehicleAvailable("V2"),
                    "Unassigned vehicle remains available");

            check(bin3.getCurrentWasteKg() == 50,
                    "Restoring completed task does not empty its bin");

            operations.completeCollection("T2");

            check(bin1.getCurrentWasteKg() == 0,
                    "Restored in-progress task can be completed");

            check(operations.isVehicleAvailable("V1"),
                    "Vehicle becomes available after completion");

            System.out.println(
                    "PASSED: Restore task statuses and assignments");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testRestoreRejectsMissingBinWithoutPartialChanges()
            throws IOException {

        Path file = Files.createTempFile("tasks-missing-bin-", ".csv");

        try {
            WasteManagementService bins = new WasteManagementService();
            bins.registerBin(new WasteBin("B1", "North", 100, 90));

            CollectionOperationsService operations =
                    new CollectionOperationsService();
            operations.registerVehicle(
                    new CollectionVehicle("V1", "REG-011", 100));

            CollectionTaskPersistenceService persistence =
                    new CollectionTaskPersistenceService();

            persistence.saveTasks(List.of(
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T1", "B1", null,
                            CollectionTask.Status.PENDING),
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T2", "UNKNOWN", null,
                            CollectionTask.Status.COMPLETED)
            ), file);

            expectIllegalArgument(
                    () -> persistence.restoreTasks(file, bins, operations),
                    "Reject missing bin");

            check(operations.getTaskCount() == 0,
                    "No tasks restored when a bin is missing");

            System.out.println(
                    "PASSED: Reject missing bin without partial restoration");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testRestoreRejectsMissingVehicleWithoutPartialChanges()
            throws IOException {

        Path file = Files.createTempFile("tasks-missing-vehicle-", ".csv");

        try {
            WasteManagementService bins = new WasteManagementService();
            bins.registerBin(new WasteBin("B1", "North", 100, 90));
            bins.registerBin(new WasteBin("B2", "South", 100, 20));

            CollectionOperationsService operations =
                    new CollectionOperationsService();
            operations.registerVehicle(
                    new CollectionVehicle("V1", "REG-021", 100));

            CollectionTaskPersistenceService persistence =
                    new CollectionTaskPersistenceService();

            persistence.saveTasks(List.of(
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T1", "B2", null,
                            CollectionTask.Status.PENDING),
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T2", "B1", "UNKNOWN",
                            CollectionTask.Status.IN_PROGRESS)
            ), file);

            expectIllegalArgument(
                    () -> persistence.restoreTasks(file, bins, operations),
                    "Reject missing vehicle");

            check(operations.getTaskCount() == 0,
                    "No tasks restored when a vehicle is missing");

            System.out.println(
                    "PASSED: Reject missing vehicle without partial restoration");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testRestoreRejectsVehicleAssignedTwice()
            throws IOException {

        Path file = Files.createTempFile("tasks-duplicate-vehicle-", ".csv");

        try {
            WasteManagementService bins = new WasteManagementService();
            bins.registerBin(new WasteBin("B1", "North", 100, 90));
            bins.registerBin(new WasteBin("B2", "South", 100, 85));

            CollectionOperationsService operations =
                    new CollectionOperationsService();
            operations.registerVehicle(
                    new CollectionVehicle("V1", "REG-031", 100));

            CollectionTaskPersistenceService persistence =
                    new CollectionTaskPersistenceService();

            persistence.saveTasks(List.of(
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T1", "B1", "V1",
                            CollectionTask.Status.IN_PROGRESS),
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T2", "B2", "V1",
                            CollectionTask.Status.IN_PROGRESS)
            ), file);

            expectIllegalArgument(
                    () -> persistence.restoreTasks(file, bins, operations),
                    "Reject duplicate vehicle assignments");

            check(operations.getTaskCount() == 0,
                    "No tasks restored for duplicate vehicle assignments");

            check(operations.isVehicleAvailable("V1"),
                    "Vehicle remains available after rejected restoration");

            System.out.println(
                    "PASSED: Reject duplicate vehicle assignments");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testRestoreRejectsExistingTaskId()
            throws IOException {

        Path file = Files.createTempFile("tasks-existing-id-", ".csv");

        try {
            WasteManagementService bins = new WasteManagementService();
            bins.registerBin(new WasteBin("B1", "North", 100, 90));

            CollectionOperationsService operations =
                    new CollectionOperationsService();

            operations.registerVehicle(
                    new CollectionVehicle("V1", "REG-041", 100));

            operations.restoreTask(
                    "T1",
                    bins.findBin("B1"),
                    null,
                    CollectionTask.Status.PENDING);

            CollectionTaskPersistenceService persistence =
                    new CollectionTaskPersistenceService();

            persistence.saveTasks(List.of(
                    new CollectionTaskPersistenceService.TaskRecord(
                            "T1", "B1", null,
                            CollectionTask.Status.PENDING)
            ), file);

            expectIllegalArgument(
                    () -> persistence.restoreTasks(file, bins, operations),
                    "Reject an existing task ID");

            check(operations.getTaskCount() == 1,
                    "Existing task remains unchanged");

            System.out.println("PASSED: Reject existing task IDs");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }

        checks++;
        System.out.println("PASSED: " + description);
    }

    private static void expectIllegalArgument(
            CheckedAction action, String description) {

        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            checks++;
            System.out.println("PASSED: " + description);
            return;
        } catch (Exception exception) {
            throw new AssertionError(
                    "FAILED: " + description
                            + " — unexpected exception.", exception);
        }

        throw new AssertionError(
                "FAILED: " + description
                        + " — exception was not thrown.");
    }
}
