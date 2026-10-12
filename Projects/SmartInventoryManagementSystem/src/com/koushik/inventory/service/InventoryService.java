
package com.koushik.inventory.service;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.storage.FileStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final List<Product> products = new ArrayList<>();
    private final FileStorage storage;

    public InventoryService() {
        this(new FileStorage());
    }

    public InventoryService(FileStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }
        this.storage = storage;
        try {
            products.addAll(storage.load());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to load inventory: " + e.getMessage(), e);
        }
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        boolean exists = products.stream()
                .anyMatch(p -> p.getProductId()
                        .equalsIgnoreCase(product.getProductId()));

        if (exists) {
            throw new IllegalArgumentException("Product ID already exists.");
        }

        List<Product> updatedProducts = new ArrayList<>(products);
        updatedProducts.add(product);
        persist(updatedProducts);
        products.add(product);
    }

    public void updateProduct(String productId, String name, String category,
                              double price, int quantity, int lowStockThreshold) {
        int productIndex = findProductIndex(productId);
        if (productIndex < 0) {
            throw new IllegalArgumentException("Product not found.");
        }

        Product updatedProduct = new Product(
                products.get(productIndex).getProductId(),
                name, category, price, quantity, lowStockThreshold);
        List<Product> updatedProducts = new ArrayList<>(products);
        updatedProducts.set(productIndex, updatedProduct);
        persist(updatedProducts);
        products.set(productIndex, updatedProduct);
    }

    public List<Product> getAllProducts() {
        return List.copyOf(products);
    }

    public Optional<Product> findProductById(String productId) {
        return products.stream()
                .filter(p -> p.getProductId().equalsIgnoreCase(productId))
                .findFirst();
    }

    public boolean deleteProduct(String productId) {
        List<Product> updatedProducts = new ArrayList<>(products);
        boolean removed = updatedProducts.removeIf(
                p -> p.getProductId().equalsIgnoreCase(productId));
        if (removed) {
            persist(updatedProducts);
            products.clear();
            products.addAll(updatedProducts);
        }
        return removed;
    }

    public List<Product> getProductsSortedByPrice() {
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice))
                .toList();
    }

    public List<Product> getLowStockProducts() {
        return products.stream()
                .filter(Product::isLowStock)
                .toList();
    }

    public double getTotalInventoryValue() {
        return products.stream()
                .mapToDouble(Product::getStockValue)
                .sum();
    }

    private int findProductIndex(String productId) {
        for (int index = 0; index < products.size(); index++) {
            if (products.get(index).getProductId().equalsIgnoreCase(productId)) {
                return index;
            }
        }
        return -1;
    }

    private void persist(List<Product> updatedProducts) {
        try {
            storage.save(updatedProducts);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to save inventory: " + e.getMessage(), e);
        }
    }
}
