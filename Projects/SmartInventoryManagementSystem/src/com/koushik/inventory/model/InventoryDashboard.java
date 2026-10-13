package com.koushik.inventory.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Encapsulates real-time inventory overview metrics.
 */
public class InventoryDashboard {

    private final int totalProducts;
    private final long totalStockQuantity;
    private final BigDecimal totalInventoryValue;
    private final int lowStockProductsCount;

    public InventoryDashboard(int totalProducts, long totalStockQuantity,
                              BigDecimal totalInventoryValue, int lowStockProductsCount) {
        if (totalProducts < 0) {
            throw new IllegalArgumentException("Total products cannot be negative.");
        }
        if (totalStockQuantity < 0) {
            throw new IllegalArgumentException("Total stock quantity cannot be negative.");
        }
        if (totalInventoryValue == null || totalInventoryValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total inventory value cannot be negative or null.");
        }
        if (lowStockProductsCount < 0) {
            throw new IllegalArgumentException("Low-stock products count cannot be negative.");
        }
        if (lowStockProductsCount > totalProducts) {
            throw new IllegalArgumentException("Low-stock products count cannot exceed total products.");
        }

        this.totalProducts = totalProducts;
        this.totalStockQuantity = totalStockQuantity;
        this.totalInventoryValue = totalInventoryValue.setScale(2, RoundingMode.HALF_UP);
        this.lowStockProductsCount = lowStockProductsCount;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public long getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public double getTotalInventoryValueAsDouble() {
        return totalInventoryValue.doubleValue();
    }

    public int getLowStockProductsCount() {
        return lowStockProductsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryDashboard that = (InventoryDashboard) o;
        return totalProducts == that.totalProducts
                && totalStockQuantity == that.totalStockQuantity
                && lowStockProductsCount == that.lowStockProductsCount
                && Objects.equals(totalInventoryValue, that.totalInventoryValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalProducts, totalStockQuantity, totalInventoryValue, lowStockProductsCount);
    }

    @Override
    public String toString() {
        return """
                ==================================
                       INVENTORY DASHBOARD
                ==================================
                Total Products       : %d
                Total Stock Quantity : %d
                Total Inventory Value: Rs. %s
                Low-Stock Products   : %d
                =================================="""
                .formatted(totalProducts, totalStockQuantity,
                        totalInventoryValue.toPlainString(), lowStockProductsCount);
    }
}
