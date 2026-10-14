
package com.koushik.waste;

public class CollectionVehicle {

    private final String vehicleId;
    private final String registrationNumber;
    private final double capacityKg;
    private boolean available;

    public CollectionVehicle(String vehicleId,
                             String registrationNumber,
                             double capacityKg) {
        if (vehicleId == null || vehicleId.isBlank()
                || registrationNumber == null
                || registrationNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID and registration are required.");
        }

        if (!Double.isFinite(capacityKg) || capacityKg <= 0) {
            throw new IllegalArgumentException(
                    "Vehicle capacity must be positive.");
        }

        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.capacityKg = capacityKg;
        this.available = true;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public double getCapacityKg() {
        return capacityKg;
    }

    public boolean isAvailable() {
        return available;
    }

    public void assignVehicle() {
        if (!available) {
            throw new IllegalStateException(
                    "Vehicle is already assigned.");
        }
        available = false;
    }

    public void releaseVehicle() {
        available = true;
    }

    @Override
    public String toString() {
        return String.format(
                "Vehicle ID: %s | Registration: %s | Capacity: %.2f kg | Available: %s",
                vehicleId, registrationNumber,
                capacityKg, available);
    }
}
