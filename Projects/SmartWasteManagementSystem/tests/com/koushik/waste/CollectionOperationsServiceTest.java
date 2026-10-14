
package com.koushik.waste;

public class CollectionOperationsServiceTest {

    private static int passed = 0;

    public static void main(String[] args) {
        testSuccessfulCollection();
        testBusyVehicleRejected();
        testInsufficientCapacityRejected();
        testBinBelowThresholdRejected();
        testUnknownVehicleRejected();
        testDuplicateTaskRejected();
        testUnknownTaskRejected();

        System.out.println();
        System.out.println("All Phase 3 tests passed: " + passed);
    }

    private static void testSuccessfulCollection() {
        CollectionOperationsService service = setup(1000);

        WasteBin bin = new WasteBin(
                "BIN001", "Main Road", 100, 90);

        CollectionTask task = service.assignCollection(
                "TASK001", bin, "VEH001");

        check(task.getStatus() == CollectionTask.Status.IN_PROGRESS,
                "Task starts successfully");

        check(!service.isVehicleAvailable("VEH001"),
                "Vehicle becomes unavailable");

        service.completeCollection("TASK001");

        check(task.getStatus() == CollectionTask.Status.COMPLETED,
                "Task completes successfully");

        check(bin.getCurrentWasteKg() == 0,
                "Bin is emptied");

        check(service.isVehicleAvailable("VEH001"),
                "Vehicle is released");

        check(service.getTaskCount() == 1,
                "Completed task remains in history");
    }

    private static void testBusyVehicleRejected() {
        CollectionOperationsService service = setup(1000);

        service.assignCollection(
                "TASK001",
                new WasteBin("BIN001", "Road A", 100, 90),
                "VEH001");

        expectThrows(IllegalStateException.class,
                () -> service.assignCollection(
                        "TASK002",
                        new WasteBin("BIN002", "Road B", 100, 90),
                        "VEH001"),
                "Reject busy vehicle");
    }

    private static void testInsufficientCapacityRejected() {
        CollectionOperationsService service = setup(50);

        expectThrows(IllegalStateException.class,
                () -> service.assignCollection(
                        "TASK001",
                        new WasteBin("BIN001", "Road A", 100, 90),
                        "VEH001"),
                "Reject insufficient vehicle capacity");

        check(service.isVehicleAvailable("VEH001"),
                "Vehicle remains available after rejection");
    }

    private static void testBinBelowThresholdRejected() {
        CollectionOperationsService service = setup(1000);

        expectThrows(IllegalStateException.class,
                () -> service.assignCollection(
                        "TASK001",
                        new WasteBin("BIN001", "Park", 100, 40),
                        "VEH001"),
                "Reject bin below collection threshold");

        check(service.isVehicleAvailable("VEH001"),
                "Vehicle remains available after threshold rejection");
    }

    private static void testUnknownVehicleRejected() {
        CollectionOperationsService service = setup(1000);

        expectThrows(IllegalArgumentException.class,
                () -> service.assignCollection(
                        "TASK001",
                        new WasteBin("BIN001", "Road", 100, 90),
                        "UNKNOWN"),
                "Reject unknown vehicle");
    }

    private static void testDuplicateTaskRejected() {
        CollectionOperationsService service = setup(1000);

        service.assignCollection(
                "TASK001",
                new WasteBin("BIN001", "Road A", 100, 90),
                "VEH001");

        expectThrows(IllegalArgumentException.class,
                () -> service.assignCollection(
                        "TASK001",
                        new WasteBin("BIN002", "Road B", 100, 90),
                        "VEH002"),
                "Reject duplicate task ID");

        check(service.isVehicleAvailable("VEH002"),
                "Unused vehicle remains available");
    }

    private static void testUnknownTaskRejected() {
        CollectionOperationsService service = setup(1000);

        expectThrows(IllegalArgumentException.class,
                () -> service.completeCollection("UNKNOWN"),
                "Reject completion of unknown task");
    }

    private static CollectionOperationsService setup(
            double capacityKg) {

        CollectionOperationsService service =
                new CollectionOperationsService();

        service.registerVehicle(new CollectionVehicle(
                "VEH001", "KA-40-AB-1234", capacityKg));

        service.registerVehicle(new CollectionVehicle(
                "VEH002", "KA-40-CD-5678", 1000));

        return service;
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }

        passed++;
        System.out.println("PASSED: " + name);
    }

    private static void expectThrows(
            Class<? extends RuntimeException> expected,
            Runnable action,
            String name) {

        try {
            action.run();
        } catch (RuntimeException exception) {
            if (expected.isInstance(exception)) {
                check(true, name);
                return;
            }

            throw new AssertionError(
                    "Wrong exception for: " + name, exception);
        }

        throw new AssertionError(
                "No exception thrown for: " + name);
    }
}
