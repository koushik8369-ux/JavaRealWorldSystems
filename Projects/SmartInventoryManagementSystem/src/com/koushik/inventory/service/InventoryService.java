package com.koushik.inventory.service;

import com.koushik.inventory.model.BestSellingProduct;
import com.koushik.inventory.model.InventoryDashboard;
import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.SalesAndPurchaseReport;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.model.TransactionType;
import com.koushik.inventory.storage.FileStorage;
import com.koushik.inventory.util.CsvUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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

    public InventoryDashboard getInventoryDashboard() {
        int totalProducts = products.size();
        long totalStockQuantity = products.stream()
                .mapToLong(Product::getQuantity)
                .sum();
        BigDecimal totalInventoryValue = products.stream()
                .map(p -> BigDecimal.valueOf(p.getPrice())
                        .multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int lowStockCount = (int) products.stream()
                .filter(Product::isLowStock)
                .count();

        return new InventoryDashboard(totalProducts, totalStockQuantity,
                totalInventoryValue, lowStockCount);
    }

    public SalesAndPurchaseReport getSalesAndPurchaseReport() {
        BigDecimal totalSalesValue = transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchaseValue = transactions.stream()
                .filter(t -> t.getType() == TransactionType.PURCHASE)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int salesCount = (int) transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .count();

        int purchaseCount = (int) transactions.stream()
                .filter(t -> t.getType() == TransactionType.PURCHASE)
                .count();

        long unitsSold = transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .mapToLong(Transaction::getQuantity)
                .sum();

        long unitsPurchased = transactions.stream()
                .filter(t -> t.getType() == TransactionType.PURCHASE)
                .mapToLong(Transaction::getQuantity)
                .sum();

        BestSellingProduct bestSelling = null;
        List<Transaction> sales = transactions.stream()
                .filter(t -> t.getType() == TransactionType.SALE)
                .toList();

        if (!sales.isEmpty()) {
            Map<String, long[]> productUnits = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Map<String, BigDecimal> productRevenue = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for (Transaction t : sales) {
                String pid = t.getProductId();
                productUnits.computeIfAbsent(pid, k -> new long[1])[0] += t.getQuantity();
                productRevenue.merge(pid, t.getTotalAmount(), BigDecimal::add);
            }

            String bestPid = null;
            long maxUnits = 0;
            BigDecimal maxRevenue = BigDecimal.ZERO;

            for (Map.Entry<String, long[]> entry : productUnits.entrySet()) {
                String pid = entry.getKey();
                long qty = entry.getValue()[0];
                BigDecimal rev = productRevenue.getOrDefault(pid, BigDecimal.ZERO);

                if (qty > maxUnits || (qty == maxUnits && rev.compareTo(maxRevenue) > 0)) {
                    maxUnits = qty;
                    maxRevenue = rev;
                    bestPid = pid;
                }
            }

            if (bestPid != null && maxUnits > 0) {
                Optional<Product> prodOpt = findProductById(bestPid);
                String name = prodOpt.map(Product::getName).orElse("Unknown / Not in catalog");
                bestSelling = new BestSellingProduct(bestPid, name, maxUnits, maxRevenue);
            }
        }

        return new SalesAndPurchaseReport(
                totalSalesValue,
                totalPurchaseValue,
                salesCount,
                purchaseCount,
                unitsSold,
                unitsPurchased,
                bestSelling
        );
    }

    public Path exportInventoryCsv() throws IOException {
        return exportInventoryCsv(Path.of("reports", "inventory.csv"));
    }

    public Path exportInventoryCsv(Path targetPath) throws IOException {
        validateExportPath(targetPath);

        Path absoluteTarget = targetPath.toAbsolutePath().normalize();
        Path parent = absoluteTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtil.toCsvRow(
                "Product ID",
                "Name",
                "Category",
                "Price",
                "Quantity",
                "Low Stock Threshold",
                "Low Stock Alert",
                "Stock Value"
        ));

        for (Product p : products) {
            BigDecimal stockValue = BigDecimal.valueOf(p.getPrice())
                    .multiply(BigDecimal.valueOf(p.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            csv.append(CsvUtil.toCsvRow(
                    p.getProductId(),
                    p.getName(),
                    p.getCategory(),
                    CsvUtil.formatMonetary(p.getPrice()),
                    Integer.toString(p.getQuantity()),
                    Integer.toString(p.getLowStockThreshold()),
                    Boolean.toString(p.isLowStock()),
                    stockValue.toPlainString()
            ));
        }

        Files.writeString(absoluteTarget, csv.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        return targetPath;
    }

    public Path exportTransactionsCsv() throws IOException {
        return exportTransactionsCsv(Path.of("reports", "transactions.csv"));
    }

    public Path exportTransactionsCsv(Path targetPath) throws IOException {
        validateExportPath(targetPath);

        Path absoluteTarget = targetPath.toAbsolutePath().normalize();
        Path parent = absoluteTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder csv = new StringBuilder();
        csv.append(CsvUtil.toCsvRow(
                "Transaction ID",
                "Timestamp",
                "Product ID",
                "Transaction Type",
                "Quantity",
                "Unit Price",
                "Total Amount"
        ));

        for (Transaction t : getTransactionHistory()) {
            csv.append(CsvUtil.toCsvRow(
                    t.getTransactionId(),
                    t.getTimestamp().toString(),
                    t.getProductId(),
                    t.getType().name(),
                    Integer.toString(t.getQuantity()),
                    CsvUtil.formatMonetary(t.getUnitPrice()),
                    t.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString()
            ));
        }

        Files.writeString(absoluteTarget, csv.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        return targetPath;
    }

    private void validateExportPath(Path targetPath) {
        if (targetPath == null) {
            throw new IllegalArgumentException("Export target path is required.");
        }

        Path absoluteTarget = targetPath.toAbsolutePath().normalize();

        if (Files.exists(absoluteTarget) && Files.isDirectory(absoluteTarget)) {
            throw new IllegalArgumentException(
                    "Export target path must be a file, not an existing directory: " + targetPath);
        }

        String fileName = absoluteTarget.getFileName().toString();
        if (fileName.equalsIgnoreCase("products.txt") || fileName.equalsIgnoreCase("transactions.txt")) {
            throw new IllegalArgumentException(
                    "Export target cannot overwrite storage file: " + targetPath);
        }

        if (storage != null) {
            if (storage.getFile() != null) {
                Path storagePath = storage.getFile().toAbsolutePath().normalize();
                if (absoluteTarget.equals(storagePath)) {
                    throw new IllegalArgumentException(
                        "Export target cannot overwrite product storage file: " + targetPath);
                }
            }
            if (storage.getTransactionFile() != null) {
                Path txnPath = storage.getTransactionFile().toAbsolutePath().normalize();
                if (absoluteTarget.equals(txnPath)) {
                    throw new IllegalArgumentException(
                        "Export target cannot overwrite transaction storage file: " + targetPath);
                }
            }
        }
    }
}
