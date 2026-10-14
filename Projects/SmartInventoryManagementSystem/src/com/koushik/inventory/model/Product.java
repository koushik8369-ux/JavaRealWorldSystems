package com.koushik.inventory.model;

import java.time.LocalDate;

public class Product {
    private final String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int lowStockThreshold;
    /** Optional expiry date. {@code null} means no expiry is tracked for this product. */
    private final LocalDate expiryDate;

    /**
     * Creates a product without an expiry date.
     * Delegates to the seven-argument constructor with {@code expiryDate = null}.
     */
    public Product(String productId, String name, String category,
                   double price, int quantity, int lowStockThreshold) {
        this(productId, name, category, price, quantity, lowStockThreshold, null);
    }

    /**
     * Creates a product with an optional expiry date.
     *
     * @param expiryDate the date on which the product expires, or {@code null} if not tracked
     */
    public Product(String productId, String name, String category,
                   double price, int quantity, int lowStockThreshold,
                   LocalDate expiryDate) {
        if (productId == null || productId.isBlank())
            throw new IllegalArgumentException("Product ID is required.");

        this.productId = productId.trim();
        setName(name);
        setCategory(category);
        setPrice(price);
        setQuantity(quantity);
        setLowStockThreshold(lowStockThreshold);
        this.expiryDate = expiryDate;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }

    /**
     * Returns the expiry date, or {@code null} if no expiry is tracked for this product.
     */
    public LocalDate getExpiryDate() { return expiryDate; }

    public final void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }

    public final void setCategory(String category) {
        if (category == null || category.isBlank())
            throw new IllegalArgumentException("Category is required.");
        this.category = category.trim();
    }

    public final void setPrice(double price) {
        if (!Double.isFinite(price) || price <= 0)
            throw new IllegalArgumentException("Price must be positive.");
        this.price = price;
    }

    public final void setQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative.");
        this.quantity = quantity;
    }

    public final void setLowStockThreshold(int threshold) {
        if (threshold < 0)
            throw new IllegalArgumentException("Threshold cannot be negative.");
        this.lowStockThreshold = threshold;
    }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public double getStockValue() {
        return price * quantity;
    }

    /**
     * Returns {@code true} when a non-null expiry date is strictly before today
     * (i.e., the product has already expired).
     * Products without an expiry date ({@code expiryDate == null}) always return {@code false}.
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Returns {@code true} when a non-null expiry date falls on today or within the next
     * {@code days} days (inclusive on both ends).
     * Products without an expiry date ({@code expiryDate == null}) always return {@code false}.
     *
     * @param days number of days to look ahead; must be non-negative
     * @throws IllegalArgumentException if {@code days} is negative
     */
    public boolean isExpiringSoon(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Days cannot be negative.");
        if (expiryDate == null)
            return false;
        LocalDate today = LocalDate.now();
        return !expiryDate.isBefore(today) && !expiryDate.isAfter(today.plusDays(days));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId.equalsIgnoreCase(product.productId);
    }

    @Override
    public int hashCode() {
        return productId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        String expiry = (expiryDate != null) ? expiryDate.toString() : "None";
        return "Product ID: " + productId
                + "\nName: " + name
                + "\nCategory: " + category
                + "\nPrice: Rs. " + price
                + "\nQuantity: " + quantity
                + "\nLow Stock: " + isLowStock()
                + "\nStock Value: Rs. " + getStockValue()
                + "\nExpiry Date: " + expiry;
    }
}