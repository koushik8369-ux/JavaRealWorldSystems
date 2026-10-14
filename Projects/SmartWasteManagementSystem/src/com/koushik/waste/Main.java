
package com.koushik.waste;

public class Main {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" SMART WASTE MANAGEMENT SYSTEM");
        System.out.println(" Phase 2: Waste Bin Management");
        System.out.println("======================================");

        WasteManagementService service =
                new WasteManagementService();

        service.registerBin(new WasteBin(
                "BIN001", "Main Road", 100.0, 85.0));

        service.registerBin(new WasteBin(
                "BIN002", "Bus Stand", 120.0, 110.0));

        service.registerBin(new WasteBin(
                "BIN003", "City Park", 80.0, 25.0));

        service.registerBin(new WasteBin(
                "BIN004", "Market Area", 150.0, 135.0));

        System.out.println("\n--- All Registered Bins ---");

        for (WasteBin bin : service.getAllBins()) {
            System.out.println(bin);
        }

        System.out.println("\nTotal bins: " + service.getBinCount());

        System.out.println("\n--- Search BIN002 ---");
        System.out.println(service.findBin("BIN002"));

        System.out.println("\n--- Collection Priority ---");

        for (WasteBin bin :
                service.getBinsNeedingCollection()) {
            System.out.printf(
                    "%s | Fill: %.1f%%%n",
                    bin.getBinId(),
                    bin.getFillPercentage());
        }

        System.out.println("\n--- Duplicate ID Validation ---");

        try {
            service.registerBin(new WasteBin(
                    "BIN001", "Duplicate Location",
                    100.0, 10.0));

            System.out.println("ERROR: Duplicate was accepted.");

        } catch (IllegalArgumentException exception) {
            System.out.println(
                    "Duplicate rejected: " + exception.getMessage());
        }

        System.out.println("\n--- Remove BIN003 ---");

        System.out.println(
                "Removed: " + service.removeBin("BIN003"));

        System.out.println(
                "Remaining bins: " + service.getBinCount());

        System.out.println("\nPhase 2 demonstration completed.");
    }
}
