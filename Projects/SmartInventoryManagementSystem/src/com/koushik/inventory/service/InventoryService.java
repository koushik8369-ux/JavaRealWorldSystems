package com.koushik.inventory.service;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.model.TransactionType;
import com.koushik.inventory.storage.FileStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final List<Product> products = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final FileStorage storage;
    private long nextTransactionNumber = 1;

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
            transactions.addAll(storage.loadTransactions());
            updateNextTransactionNumber(transactions);
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
        if (productId == null) {
            return Optional.empty();
        }
        return products.stream()
                .filter(p -> p.getProductId().equalsIgnoreCase(productId.trim()))
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

    public Transaction purchaseStock(String productId, int quantity) {
        return purchaseStock(productId, quantity, LocalDateTime.now());
    }

    public Transaction purchaseStock(String productId, int quantity, LocalDateTime timestamp) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be positive.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required.");
        }

        int productIndex = findProductIndex(productId);
        if (productIndex < 0) {
            throw new IllegalArgumentException("Product not found.");
        }

        Product current = products.get(productIndex);
        int newQuantity;
        try {
            newQuantity = Math.addExact(current.getQuantity(), quantity);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Integer overflow: stock quantity exceeds maximum allowed.");
        }

        double unitPrice = current.getPrice();
        BigDecimal totalAmount = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
        String transactionId = generateTransactionId();

        Transaction transaction = new Transaction(
                transactionId,
                current.getProductId(),
                TransactionType.PURCHASE,
                quantity,
                unitPrice,
                totalAmount,
                timestamp);

        Product updatedProduct = new Product(
                current.getProductId(),
                current.getName(),
                current.getCategory(),
                current.getPrice(),
                newQuantity,
                current.getLowStockThreshold());

        List<Product> updatedProducts = new ArrayList<>(products);
        updatedProducts.set(productIndex, updatedProduct);

        List<Transaction> updatedTransactions = new ArrayList<>(transactions);
        updatedTransactions.add(transaction);

        persistAll(updatedProducts, updatedTransactions);

        products.set(productIndex, updatedProduct);
        transactions.add(transaction);

        return transaction;
    }

    public Transaction sellStock(String productId, int quantity) {
        return sellStock(productId, quantity, LocalDateTime.now());
    }

    public Transaction sellStock(String productId, int quantity, LocalDateTime timestamp) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sale quantity must be positive.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required.");
        }

        int productIndex = findProductIndex(productId);
        if (productIndex < 0) {
            throw new IllegalArgumentException("Product not found.");
        }

        Product current = products.get(productIndex);
        if (quantity > current.getQuantity()) {
            throw new IllegalArgumentException(
                    "Sale quantity exceeds available stock (" + current.getQuantity() + ").");
        }

        int newQuantity = current.getQuantity() - quantity;
        double unitPrice = current.getPrice();
        BigDecimal totalAmount = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
        String transactionId = generateTransactionId();

        Transaction transaction = new Transaction(
                transactionId,
                current.getProductId(),
                TransactionType.SALE,
                quantity,
                unitPrice,
                totalAmount,
                timestamp);

        Product updatedProduct = new Product(
                current.getProductId(),
                current.getName(),
                current.getCategory(),
                current.getPrice(),
                newQuantity,
                current.getLowStockThreshold());

        List<Product> updatedProducts = new ArrayList<>(products);
        updatedProducts.set(productIndex, updatedProduct);

        List<Transaction> updatedTransactions = new ArrayList<>(transactions);
        updatedTransactions.add(transaction);

        persistAll(updatedProducts, updatedTransactions);

        products.set(productIndex, updatedProduct);
        transactions.add(transaction);

        return transaction;
    }

    public List<Transaction> getTransactionHistory() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp)
                        .thenComparing(Transaction::getTransactionId))
                .toList();
    }

    public Optional<Transaction> findTransactionById(String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }
        return transactions.stream()
                .filter(t -> t.getTransactionId().equalsIgnoreCase(transactionId.trim()))
                .findFirst();
    }

    private int findProductIndex(String productId) {
        if (productId == null) {
            return -1;
        }
        for (int index = 0; index < products.size(); index++) {
            if (products.get(index).getProductId().equalsIgnoreCase(productId.trim())) {
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

    private void persistAll(List<Product> updatedProducts, List<Transaction> updatedTransactions) {
        try {
            storage.saveAll(updatedProducts, updatedTransactions);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to save inventory and transactions: " + e.getMessage(), e);
        }
    }

    private void updateNextTransactionNumber(List<Transaction> existingTransactions) {
        long max = 0;
        for (Transaction t : existingTransactions) {
            String id = t.getTransactionId();
            if (id.toUpperCase().startsWith("TXN-")) {
                try {
                    long num = Long.parseLong(id.substring(4));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        this.nextTransactionNumber = Math.max(1, max + 1);
    }

    private synchronized String generateTransactionId() {
        while (true) {
            String candidate = String.format("TXN-%05d", nextTransactionNumber++);
            if (findTransactionById(candidate).isEmpty()) {
                return candidate;
            }
        }
    }
}
