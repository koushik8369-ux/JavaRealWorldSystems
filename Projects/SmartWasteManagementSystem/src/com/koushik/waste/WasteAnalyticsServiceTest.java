
package com.koushik.waste;

public class WasteAnalyticsServiceTest {

    private static int checks = 0;

    public static void main(String[] args) {
        testBinStatistics();
        testEmptyBinStatistics();
        testTaskAndVehicleStatistics();
        testGeneratedReport();

        System.out.println(
                "\nAll Phase 5 analytics tests passed: " + checks);
    }

    private static void testBinStatistics() {
        WasteManagementService wasteService =
                new WasteManagementService();
        CollectionOperationsService operationsService =
                new CollectionOperationsService();

        WasteBin bin1 = new WasteBin("B1", "Market", 100.0, 0.0);
        WasteBin bin2 = new WasteBin("B2", "Park", 200.0, 0.0);
        WasteBin bin3 = new WasteBin("B3", "Hospital", 50.0, 0.0);

        bin1.addWaste(90.0);
        bin2.addWaste(100.0);
        bin3.addWaste(45.0);

        wasteService.registerBin(bin1);
        wasteService.registerBin(bin2);
        wasteService.registerBin(bin3);

        WasteAnalyticsService analytics =
                new WasteAnalyticsService(
                        wasteService, operationsService);

        check(analytics.getTotalBins() == 3,
                "Count registered bins");
        check(closeTo(analytics.getTotalCapacityKg(), 350.0),
                "Calculate total bin capacity");
        check(closeTo(analytics.getCurrentWasteKg(), 235.0),
                "Calculate current waste");
        check(closeTo(
                        analytics.getAverageFillPercentage(),
                        (90.0 + 50.0 + 90.0) / 3.0),
                "Calculate average bin fill percentage");
        check(analytics.getBinsNeedingCollectionCount() == 2,
                "Count bins needing collection");

        System.out.println("Bin analytics tests passed.");
    }

    private static void testEmptyBinStatistics() {
        WasteManagementService wasteService =
                new WasteManagementService();
        CollectionOperationsService operationsService =
                new CollectionOperationsService();

        WasteAnalyticsService analytics =
                new WasteAnalyticsService(
                        wasteService, operationsService);

        check(analytics.getTotalBins() == 0,
                "Handle zero registered bins");
        check(closeTo(analytics.getAverageFillPercentage(), 0.0),
                "Average fill is zero when there are no bins");
        check(closeTo(analytics.getCurrentWasteKg(), 0.0),
                "Current waste is zero when there are no bins");

        System.out.println("Empty-bin analytics tests passed.");
    }

    private static void testTaskAndVehicleStatistics() {
        WasteManagementService wasteService =
                new WasteManagementService();
        CollectionOperationsService operationsService =
                new CollectionOperationsService();

        WasteBin bin1 = new WasteBin("B10", "Market", 100.0, 0.0);
        WasteBin bin2 = new WasteBin("B20", "Park", 100.0, 0.0);

        bin1.addWaste(90.0);
        bin2.addWaste(90.0);

        wasteService.registerBin(bin1);
        wasteService.registerBin(bin2);

        CollectionVehicle vehicle1 =
                new CollectionVehicle("V1", "KA01AA1111", 150.0);
        CollectionVehicle vehicle2 =
                new CollectionVehicle("V2", "KA01BB2222", 150.0);

        operationsService.registerVehicle(vehicle1);
        operationsService.registerVehicle(vehicle2);

        operationsService.assignCollection("T1", bin1, "V1");
        operationsService.completeCollection("T1");
        operationsService.assignCollection("T2", bin2, "V2");

        WasteAnalyticsService analytics =
                new WasteAnalyticsService(
                        wasteService, operationsService);

        check(analytics.getPendingTaskCount() == 0,
                "Count pending tasks");
        check(analytics.getInProgressTaskCount() == 1,
                "Count in-progress tasks");
        check(analytics.getCompletedTaskCount() == 1,
                "Count completed tasks");
        check(analytics.getTotalVehicles() == 2,
                "Count registered vehicles");
        check(analytics.getAvailableVehicleCount() == 1,
                "Count available vehicles");
        check(analytics.getBusyVehicleCount() == 1,
                "Count busy vehicles");
        check(closeTo(analytics.getCurrentWasteKg(), 90.0),
                "Reflect bin emptying after completed collection");

        System.out.println("Task and vehicle analytics tests passed.");
    }

    private static void testGeneratedReport() {
        WasteManagementService wasteService =
                new WasteManagementService();
        CollectionOperationsService operationsService =
                new CollectionOperationsService();

        WasteBin bin = new WasteBin("B30", "City Park", 100.0, 0.0);
        bin.addWaste(85.0);
        wasteService.registerBin(bin);

        CollectionVehicle vehicle =
                new CollectionVehicle("V3", "KA02CC3333", 150.0);
        operationsService.registerVehicle(vehicle);

        WasteAnalyticsService analytics =
                new WasteAnalyticsService(
                        wasteService, operationsService);

        String report = analytics.generateReport();

        check(report.contains("SMART WASTE MANAGEMENT REPORT"),
                "Include report heading");
        check(report.contains("Current waste remaining"),
                "Include current waste statistic");
        check(report.contains("COLLECTION TASK STATISTICS"),
                "Include collection task section");
        check(report.contains("VEHICLE STATISTICS"),
                "Include vehicle section");

        System.out.println("Report generation tests passed.");
    }

    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < 0.0001;
    }

    private static void check(boolean condition, String description) {
        checks++;

        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }

        System.out.println("PASSED: " + description);
    }
}
