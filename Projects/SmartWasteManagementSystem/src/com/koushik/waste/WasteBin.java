package com.koushik.waste;

public class WasteBin {

    private final String binId;
    private String location;
    private double capacityKg;
    private double currentWasteKg;

    public WasteBin(String binId, String location,
                    double capacityKg, double currentWasteKg) {

        if (binId == null || binId.isBlank()) {
            throw new IllegalArgumentException("Bin ID cannot be empty.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be empty.");
        }
        if (!Double.isFinite(capacityKg) || capacityKg <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        if (!Double.isFinite(currentWasteKg)
                || currentWasteKg < 0
                || currentWasteKg > capacityKg) {
            throw new IllegalArgumentException(
                    "Waste must be between 0 and capacity.");
        }

        this.binId = binId;
        this.location = location;
        this.capacityKg = capacityKg;
        this.currentWasteKg = currentWasteKg;
    }

    public String getBinId() {
        return binId;
    }

    public String getLocation() {
        return location;
    }

    public double getCapacityKg() {
        return capacityKg;
    }

    public double getCurrentWasteKg() {
        return currentWasteKg;
    }

    public double getFillPercentage() {
        return (currentWasteKg / capacityKg) * 100.0;
    }

    public boolean needsCollection() {
        return getFillPercentage() >= 80.0;
    }

    public void addWaste(double weightKg) {
        if (!Double.isFinite(weightKg) || weightKg <= 0) {
            throw new IllegalArgumentException(
                    "Waste weight must be positive.");
        }
        if (currentWasteKg + weightKg > capacityKg) {
            throw new IllegalArgumentException(
                    "Adding this waste would exceed bin capacity.");
        }

        currentWasteKg += weightKg;
    }

    public void emptyBin() {
        currentWasteKg = 0;
    }

    public void setLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location cannot be empty.");
        }
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format(
                "Bin ID: %s | Location: %s | Waste: %.2f/%.2f kg | Fill: %.1f%%",
                binId, location, currentWasteKg,
                capacityKg, getFillPercentage());
    }
}