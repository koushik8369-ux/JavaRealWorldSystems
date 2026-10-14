
package com.koushik.waste;

import java.util.List;

public class WasteManagementServiceTest {

    private static int passed = 0;

    public static void main(String[] args) {

        testRegisterBins();
        testFindBin();
        testDuplicateBinRejected();
        testMissingBinRejected();
        testCollectionPriority();
        testRemoveBin();
        testNullBinRejected();

        System.out.println();
        System.out.println("All Phase 2 tests passed: " + passed);
    }

    private static void testRegisterBins() {
        WasteManagementService service = new WasteManagementService();

        service.registerBin(
                new WasteBin("BIN001", "Main Road", 100, 50));
        service.registerBin(
                new WasteBin("BIN002", "Bus Stand", 100, 90));

        check(service.getBinCount() == 2,
                "Register multiple bins");
    }

    private static void testFindBin() {
        WasteManagementService service = new WasteManagementService();
        WasteBin bin = new WasteBin("BIN001", "Main Road", 100, 50);

        service.registerBin(bin);

        check(service.findBin("BIN001") == bin,
                "Find bin by ID");
    }

    private static void testDuplicateBinRejected() {
        WasteManagementService service = new WasteManagementService();

        service.registerBin(
                new WasteBin("BIN001", "Main Road", 100, 50));

        expectThrows(IllegalArgumentException.class,
                () -> service.registerBin(
                        new WasteBin("BIN001", "Market", 100, 20)),
                "Reject duplicate bin ID");
    }

    private static void testMissingBinRejected() {
        WasteManagementService service = new WasteManagementService();

        expectThrows(IllegalArgumentException.class,
                () -> service.findBin("UNKNOWN"),
                "Reject search for missing bin");
    }

    private static void testCollectionPriority() {
        WasteManagementService service = new WasteManagementService();

        service.registerBin(
                new WasteBin("BIN001", "Main Road", 100, 85));
        service.registerBin(
                new WasteBin("BIN002", "Bus Stand", 120, 110));
        service.registerBin(
                new WasteBin("BIN003", "City Park", 80, 25));
        service.registerBin(
                new WasteBin("BIN004", "Market Area", 150, 135));

        List<WasteBin> priority =
                service.getBinsNeedingCollection();

        check(priority.size() == 3,
                "Identify bins needing collection");

        check(priority.get(0).getBinId().equals("BIN002"),
                "Highest fill percentage first");

        check(priority.get(1).getBinId().equals("BIN004"),
                "Second-highest fill percentage");

        check(priority.get(2).getBinId().equals("BIN001"),
                "Third-highest fill percentage");
    }

    private static void testRemoveBin() {
        WasteManagementService service = new WasteManagementService();

        service.registerBin(
                new WasteBin("BIN001", "Main Road", 100, 50));

        check(service.removeBin("BIN001"),
                "Remove existing bin");

        check(service.getBinCount() == 0,
                "Bin count after removal");

        check(!service.removeBin("BIN001"),
                "Removing absent bin returns false");
    }

    private static void testNullBinRejected() {
        WasteManagementService service = new WasteManagementService();

        expectThrows(IllegalArgumentException.class,
                () -> service.registerBin(null),
                "Reject null bin");
    }

    private static void check(boolean condition, String testName) {
        if (!condition) {
            throw new AssertionError("FAILED: " + testName);
        }

        passed++;
        System.out.println("PASSED: " + testName);
    }

    private static void expectThrows(
            Class<? extends RuntimeException> expected,
            Runnable action,
            String testName) {

        try {
            action.run();
        } catch (RuntimeException exception) {
            if (expected.isInstance(exception)) {
                check(true, testName);
                return;
            }

            throw new AssertionError(
                    "FAILED: " + testName, exception);
        }

        throw new AssertionError(
                "FAILED: " + testName + " — no exception thrown");
    }
}
