package com.koushik.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable inventory transaction (PURCHASE or SALE).
 */
public class Transaction {

    private final String transactionId;
    private final String productId;
    private final TransactionType type;
    private final int quantity;
    private final double unitPrice;
    private final BigDecimal totalAmount;
    private final LocalDateTime timestamp;

    public Transaction(String transactionId, String productId, TransactionType type,
                       int quantity, double unitPrice, BigDecimal totalAmount,
                       LocalDateTime timestamp) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID is required.");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (!Double.isFinite(unitPrice) || unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be positive and finite.");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required.");
        }

        this.transactionId = transactionId.trim();
        this.productId = productId.trim();
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
    }

    public Transaction(String transactionId, String productId, TransactionType type,
                       int quantity, double unitPrice, LocalDateTime timestamp) {
        this(transactionId, productId, type, quantity, unitPrice,
                calculateTotalAmount(unitPrice, quantity), timestamp);
    }

    public static BigDecimal calculateTotalAmount(double unitPrice, int quantity) {
        if (!Double.isFinite(unitPrice) || unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be positive and finite.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        return BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionType getTransactionType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getTotal() {
        return totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getTransactionTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return transactionId.equalsIgnoreCase(that.transactionId);
    }

    @Override
    public int hashCode() {
        return transactionId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Transaction ID: " + transactionId
                + "\nTimestamp: " + timestamp
                + "\nProduct ID: " + productId
                + "\nTransaction Type: " + type
                + "\nQuantity: " + quantity
                + "\nUnit Price: Rs. " + String.format("%.2f", unitPrice)
                + "\nTotal Amount: Rs. " + totalAmount.toPlainString();
    }
}
