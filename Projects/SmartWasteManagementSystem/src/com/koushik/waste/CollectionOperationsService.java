
package com.koushik.waste;

import java.util.LinkedHashMap;
import java.util.Map;

public class CollectionOperationsService {

    private final Map<String, CollectionVehicle> vehicles =
            new LinkedHashMap<>();

    private final Map<String, CollectionTask> tasks =
            new LinkedHashMap<>();

    private final Map<String, CollectionVehicle> assignedVehicles =
            new LinkedHashMap<>();

    public void registerVehicle(CollectionVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null.");
        }

        if (vehicles.containsKey(vehicle.getVehicleId())) {
            throw new IllegalArgumentException(
                    "Duplicate vehicle ID: " + vehicle.getVehicleId());
        }

        vehicles.put(vehicle.getVehicleId(), vehicle);
    }

    public CollectionTask assignCollection(
            String taskId, WasteBin bin, String vehicleId) {

        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException(
                    "Task ID cannot be empty.");
        }

        if (bin == null) {
            throw new IllegalArgumentException(
                    "Waste bin cannot be null.");
        }

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be empty.");
        }

        if (tasks.containsKey(taskId)) {
            throw new IllegalArgumentException(
                    "Duplicate task ID: " + taskId);
        }

        CollectionVehicle vehicle = vehicles.get(vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle not found: " + vehicleId);
        }

        if (!vehicle.isAvailable()) {
            throw new IllegalStateException(
                    "Vehicle is already assigned.");
        }

        if (!bin.needsCollection()) {
            throw new IllegalStateException(
                    "Bin does not require collection yet.");
        }

        if (bin.getCurrentWasteKg() > vehicle.getCapacityKg()) {
            throw new IllegalStateException(
                    "Vehicle capacity is insufficient.");
        }

        CollectionTask task = new CollectionTask(taskId, bin);

        vehicle.assignVehicle();

        try {
            task.startTask();
            tasks.put(taskId, task);
            assignedVehicles.put(taskId, vehicle);
        } catch (RuntimeException exception) {
            tasks.remove(taskId);
            assignedVehicles.remove(taskId);
            vehicle.releaseVehicle();
            throw exception;
        }

        return task;
    }

    public void completeCollection(String taskId) {
        CollectionTask task = findTask(taskId);

        if (task.getStatus() != CollectionTask.Status.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Task is not in progress.");
        }

        CollectionVehicle vehicle = assignedVehicles.get(taskId);

        if (vehicle == null) {
            throw new IllegalStateException(
                    "No vehicle is assigned to task: " + taskId);
        }

        task.completeTask();
        vehicle.releaseVehicle();
        assignedVehicles.remove(taskId);
    }

    public CollectionTask findTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException(
                    "Task ID cannot be empty.");
        }

        CollectionTask task = tasks.get(taskId);

        if (task == null) {
            throw new IllegalArgumentException(
                    "Task not found: " + taskId);
        }

        return task;
    }

    public boolean isVehicleAvailable(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be empty.");
        }

        CollectionVehicle vehicle = vehicles.get(vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle not found: " + vehicleId);
        }

        return vehicle.isAvailable();
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public int getVehicleCount() {
        return vehicles.size();
    }
}
