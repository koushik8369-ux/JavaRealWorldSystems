
package com.koushik.inventory.service;

import com.koushik.inventory.model.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final List<Product> products = new ArrayList<>();

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

        products.add(product);
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
        return products.removeIf(
                p -> p.getProductId().equalsIgnoreCase(productId));
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
}
