package com.koushik.inventory.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Encapsulates aggregated sales and purchase metrics.
 */
public class SalesAndPurchaseReport {

    private final BigDecimal totalSalesValue;
    private final BigDecimal totalPurchaseValue;
    private final int salesTransactionCount;
    private final int purchaseTransactionCount;
    private final long totalUnitsSold;
    private final long totalUnitsPurchased;
    private final BestSellingProduct bestSellingProduct;

    public SalesAndPurchaseReport(BigDecimal totalSalesValue, BigDecimal totalPurchaseValue,
                                  int salesTransactionCount, int purchaseTransactionCount,
                                  long totalUnitsSold, long totalUnitsPurchased,
                                  BestSellingProduct bestSellingProduct) {
        if (totalSalesValue == null || totalSalesValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total sales value cannot be negative or null.");
        }
        if (totalPurchaseValue == null || totalPurchaseValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total purchase value cannot be negative or null.");
        }
        if (salesTransactionCount < 0) {
            throw new IllegalArgumentException("Sales transaction count cannot be negative.");
        }
        if (purchaseTransactionCount < 0) {
            throw new IllegalArgumentException("Purchase transaction count cannot be negative.");
        }
        if (totalUnitsSold < 0) {
            throw new IllegalArgumentException("Total units sold cannot be negative.");
        }
        if (totalUnitsPurchased < 0) {
            throw new IllegalArgumentException("Total units purchased cannot be negative.");
        }

        this.totalSalesValue = totalSalesValue.setScale(2, RoundingMode.HALF_UP);
        this.totalPurchaseValue = totalPurchaseValue.setScale(2, RoundingMode.HALF_UP);
        this.salesTransactionCount = salesTransactionCount;
        this.purchaseTransactionCount = purchaseTransactionCount;
        this.totalUnitsSold = totalUnitsSold;
        this.totalUnitsPurchased = totalUnitsPurchased;
        this.bestSellingProduct = bestSellingProduct;
    }

    public BigDecimal getTotalSalesValue() {
        return totalSalesValue;
    }

    public BigDecimal getTotalPurchaseValue() {
        return totalPurchaseValue;
    }

    public int getSalesTransactionCount() {
        return salesTransactionCount;
    }

    public int getPurchaseTransactionCount() {
        return purchaseTransactionCount;
    }

    public long getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public long getTotalUnitsPurchased() {
        return totalUnitsPurchased;
    }

    public Optional<BestSellingProduct> getBestSellingProduct() {
        return Optional.ofNullable(bestSellingProduct);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesAndPurchaseReport that = (SalesAndPurchaseReport) o;
        return salesTransactionCount == that.salesTransactionCount
                && purchaseTransactionCount == that.purchaseTransactionCount
                && totalUnitsSold == that.totalUnitsSold
                && totalUnitsPurchased == that.totalUnitsPurchased
                && Objects.equals(totalSalesValue, that.totalSalesValue)
                && Objects.equals(totalPurchaseValue, that.totalPurchaseValue)
                && Objects.equals(bestSellingProduct, that.bestSellingProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSalesValue, totalPurchaseValue, salesTransactionCount,
                purchaseTransactionCount, totalUnitsSold, totalUnitsPurchased, bestSellingProduct);
    }

    @Override
    public String toString() {
        String bestSellerStr = bestSellingProduct != null
                ? bestSellingProduct.toString()
                : "None (no sales recorded)";

        return """
                ==================================
                    SALES & PURCHASE REPORT
                ==================================
                Sales Transactions    : %d
                Total Units Sold      : %d
                Total Sales Revenue   : Rs. %s
                ----------------------------------
                Purchase Transactions : %d
                Total Units Purchased : %d
                Total Purchase Cost   : Rs. %s
                ----------------------------------
                Best-Selling Product  : %s
                =================================="""
                .formatted(salesTransactionCount, totalUnitsSold, totalSalesValue.toPlainString(),
                        purchaseTransactionCount, totalUnitsPurchased, totalPurchaseValue.toPlainString(),
                        bestSellerStr);
    }
}
