package com.koushik.inventory.model;

public class Product {
    private final String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int lowStockThreshold;

    public Product(String productId, String name, String category,
                   double price, int quantity, int lowStockThreshold) {
        if (productId == null || productId.isBlank())
            throw new IllegalArgumentException("Product ID is required.");

        this.productId = productId.trim();
        setName(name);
        setCategory(category);
        setPrice(price);
        setQuantity(quantity);
        setLowStockThreshold(lowStockThreshold);
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }

    public void setCategory(String category) {
        if (category == null || category.isBlank())
            throw new IllegalArgumentException("Category is required.");
        this.category = category.trim();
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price <= 0)
            throw new IllegalArgumentException("Price must be positive.");
        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative.");
        this.quantity = quantity;
    }

    public void setLowStockThreshold(int threshold) {
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

    @Override
    public String toString() {
        return "Product ID: " + productId
                + "\nName: " + name
                + "\nCategory: " + category
                + "\nPrice: Rs. " + price
                + "\nQuantity: " + quantity
                + "\nLow Stock: " + isLowStock()
                + "\nStock Value: Rs. " + getStockValue();
    }
}
