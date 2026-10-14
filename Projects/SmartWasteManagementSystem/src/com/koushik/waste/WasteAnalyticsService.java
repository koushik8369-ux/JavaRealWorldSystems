
package com.koushik.waste;

import java.util.List;
import java.util.Locale;

public class WasteAnalyticsService {

    private final WasteManagementService wasteService;
    private final CollectionOperationsService operationsService;

    public WasteAnalyticsService(
            WasteManagementService wasteService,
            CollectionOperationsService operationsService) {

        if (wasteService == null || operationsService == null) {
            throw new IllegalArgumentException(
                    "Analytics services cannot be null.");
        }

        this.wasteService = wasteService;
        this.operationsService = operationsService;
    }

    public int getTotalBins() {
        return wasteService.getBinCount();
    }

    public double getTotalCapacityKg() {
        double total = 0.0;

        for (WasteBin bin : wasteService.getAllBins()) {
            total += bin.getCapacityKg();
        }

        return total;
    }

    public double getCurrentWasteKg() {
        double total = 0.0;

        for (WasteBin bin : wasteService.getAllBins()) {
            total += bin.getCurrentWasteKg();
        }

        return total;
    }

    public double getAverageFillPercentage() {
        List<WasteBin> bins = wasteService.getAllBins();

        if (bins.isEmpty()) {
            return 0.0;
        }

        double totalPercentage = 0.0;

        for (WasteBin bin : bins) {
            totalPercentage += bin.getFillPercentage();
        }

        return totalPercentage / bins.size();
    }

    public int getBinsNeedingCollectionCount() {
        return wasteService.getBinsNeedingCollection().size();
    }

    public int getPendingTaskCount() {
        return countTasks(CollectionTask.Status.PENDING);
    }

    public int getInProgressTaskCount() {
        return countTasks(CollectionTask.Status.IN_PROGRESS);
    }

    public int getCompletedTaskCount() {
        return countTasks(CollectionTask.Status.COMPLETED);
    }

    private int countTasks(CollectionTask.Status expectedStatus) {
        int count = 0;

        for (CollectionTask task : operationsService.getAllTasks()) {
            if (task.getStatus() == expectedStatus) {
                count++;
            }
        }

        return count;
    }

    public int getTotalVehicles() {
        return operationsService.getVehicleCount();
    }

    public int getAvailableVehicleCount() {
        int count = 0;

        for (CollectionVehicle vehicle
                : operationsService.getAllVehicles()) {
            if (vehicle.isAvailable()) {
                count++;
            }
        }

        return count;
    }

    public int getBusyVehicleCount() {
        return getTotalVehicles() - getAvailableVehicleCount();
    }

    public String generateReport() {
        return String.format(
                Locale.ROOT,
                """
                ========================================
                   SMART WASTE MANAGEMENT REPORT
                ========================================

                WASTE BIN STATISTICS
                Total registered bins       : %d
                Total bin capacity           : %.2f kg
                Current waste remaining      : %.2f kg
                Average bin fill             : %.2f%%
                Bins needing collection      : %d

                COLLECTION TASK STATISTICS
                Pending tasks                : %d
                In-progress tasks             : %d
                Completed tasks               : %d
                Total tasks                   : %d

                VEHICLE STATISTICS
                Total vehicles                : %d
                Available vehicles            : %d
                Busy vehicles                 : %d

                ========================================
                """,
                getTotalBins(),
                getTotalCapacityKg(),
                getCurrentWasteKg(),
                getAverageFillPercentage(),
                getBinsNeedingCollectionCount(),
                getPendingTaskCount(),
                getInProgressTaskCount(),
                getCompletedTaskCount(),
                operationsService.getTaskCount(),
                getTotalVehicles(),
                getAvailableVehicleCount(),
                getBusyVehicleCount());
    }
}
