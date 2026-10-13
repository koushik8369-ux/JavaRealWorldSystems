package com.koushik.inventory.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents the top-selling product determined from transaction history.
 */
public class BestSellingProduct {

    private final String productId;
    private final String productName;
    private final long unitsSold;
    private final BigDecimal totalSalesRevenue;

    public BestSellingProduct(String productId, String productName,
                              long unitsSold, BigDecimal totalSalesRevenue) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (unitsSold <= 0) {
            throw new IllegalArgumentException("Units sold must be positive.");
        }
        if (totalSalesRevenue == null || totalSalesRevenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total sales revenue cannot be negative or null.");
        }

        this.productId = productId.trim();
        this.productName = (productName == null || productName.isBlank())
                ? "Unknown / Not in catalog"
                : productName.trim();
        this.unitsSold = unitsSold;
        this.totalSalesRevenue = totalSalesRevenue.setScale(2, RoundingMode.HALF_UP);
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public long getUnitsSold() {
        return unitsSold;
    }

    public BigDecimal getTotalSalesRevenue() {
        return totalSalesRevenue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BestSellingProduct that = (BestSellingProduct) o;
        return unitsSold == that.unitsSold
                && productId.equalsIgnoreCase(that.productId)
                && Objects.equals(totalSalesRevenue, that.totalSalesRevenue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId.toLowerCase(), unitsSold, totalSalesRevenue);
    }

    @Override
    public String toString() {
        return "%s (%s) - %d units sold (Revenue: Rs. %s)"
                .formatted(productId, productName, unitsSold, totalSalesRevenue.toPlainString());
    }
}
