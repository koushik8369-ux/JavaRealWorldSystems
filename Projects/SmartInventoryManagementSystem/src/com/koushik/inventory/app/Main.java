package com.koushik.inventory.app;

import com.koushik.inventory.model.InventoryDashboard;
import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.SalesAndPurchaseReport;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.service.InventoryService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        InventoryService inventory;
        try {
            inventory = new InventoryService();
        } catch (UncheckedIOException e) {
            System.out.println("Unable to start inventory: " + e.getMessage());
            return;
        }

        boolean running = true;

        System.out.println("==================================");
        System.out.println("  SMART INVENTORY MANAGEMENT");
        System.out.println("==================================");

        while (running) {
            displayMenu();

            int choice = readInt("Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> addProduct(inventory);
                    case 2 -> viewProducts(inventory.getAllProducts());
                    case 3 -> searchProduct(inventory);
                    case 4 -> updateProduct(inventory);
                    case 5 -> deleteProduct(inventory);
                    case 6 -> sortProducts(inventory);
                    case 7 -> showLowStock(inventory);
                    case 8 -> showInventoryValue(inventory);
                    case 9 -> purchaseStock(inventory);
                    case 10 -> sellStock(inventory);
                    case 11 -> viewTransactionHistory(inventory);
                    case 12 -> showDashboard(inventory);
                    case 13 -> showSalesReport(inventory);
                    case 14 -> exportInventoryCsv(inventory);
                    case 15 -> exportTransactionsCsv(inventory);
                    case 0 -> {
                        running = false;
                        System.out.println("Thank you for using Smart Inventory!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (UncheckedIOException e) {
                System.out.println("Storage error: " + e.getMessage());
            }

            System.out.println();
        }

        SCANNER.close();
    }

    private static void displayMenu() {
        System.out.println("""

                ---------- MAIN MENU ----------
                1. Add Product
                2. View All Products
                3. Search Product
                4. Update Product
                5. Delete Product
                6. Sort Products by Price
                7. Low Stock Alerts
                8. Total Inventory Value
                9. Purchase Stock
                10. Sell Stock
                11. View Transaction History
                12. Inventory Dashboard
                13. Sales and Purchase Reports
                14. Export Inventory to CSV
                15. Export Transactions to CSV
                0. Exit
                ------------------------------
                """);
    }

    private static void addProduct(InventoryService inventory) {
        System.out.println("\n--- ADD PRODUCT ---");

        String id = readText("Product ID: ");
        String name = readText("Product name: ");
        String category = readText("Category: ");
        double price = readDouble("Price (Rs.): ");
        int quantity = readInt("Quantity: ");
        int threshold = readInt("Low-stock threshold: ");

        Product product = new Product(
                id, name, category, price, quantity, threshold);

        inventory.addProduct(product);

        System.out.println("Product added successfully!");
    }

    private static void viewProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println("\n--- PRODUCT LIST ---");

        for (Product product : products) {
            System.out.println(product);
            System.out.println("------------------------------");
        }
    }

    private static void searchProduct(InventoryService inventory) {
        String id = readText("Enter product ID to search: ");

        Optional<Product> result = inventory.findProductById(id);

        if (result.isPresent()) {
            System.out.println(result.get());
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void updateProduct(InventoryService inventory) {
        String id = readText("Enter product ID to update: ");

        Optional<Product> result = inventory.findProductById(id);

        if (result.isEmpty()) {
            System.out.println("Product not found.");
            return;
        }

        Product product = result.get();

        System.out.println("Current details:");
        System.out.println(product);
        System.out.println("\nEnter the new details:");

        String name = readText("New name: ");
        String category = readText("New category: ");
        double price = readDouble("New price (Rs.): ");
        int quantity = readInt("New quantity: ");
        int threshold = readInt("New low-stock threshold: ");

        inventory.updateProduct(
                id, name, category, price, quantity, threshold);

        System.out.println("Product updated successfully!");
    }

    private static void deleteProduct(InventoryService inventory) {
        String id = readText("Enter product ID to delete: ");

        if (inventory.deleteProduct(id)) {
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void sortProducts(InventoryService inventory) {
        System.out.println("Products sorted by price (low to high):");

        viewProducts(inventory.getProductsSortedByPrice());
    }

    private static void showLowStock(InventoryService inventory) {
        System.out.println("\n--- LOW STOCK ALERTS ---");

        viewProducts(inventory.getLowStockProducts());
    }

    private static void showInventoryValue(InventoryService inventory) {
        System.out.printf(
                "Total inventory value: Rs. %.2f%n",
                inventory.getTotalInventoryValue());
    }

    private static void purchaseStock(InventoryService inventory) {
        System.out.println("\n--- PURCHASE STOCK ---");

        String id = readText("Enter product ID: ");
        Optional<Product> result = inventory.findProductById(id);

        if (result.isEmpty()) {
            System.out.println("Error: Product not found.");
            return;
        }

        Product product = result.get();
        System.out.println("Product: " + product.getName());
        System.out.println("Current Stock: " + product.getQuantity());
        System.out.printf("Current Price: Rs. %.2f%n", product.getPrice());

        int quantity = readInt("Enter purchase quantity: ");
        if (quantity <= 0) {
            System.out.println("Error: Purchase quantity must be positive.");
            return;
        }

        try {
            Transaction transaction = inventory.purchaseStock(id, quantity);
            displayReceipt(transaction, product.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Purchase failed: " + e.getMessage());
        }
    }

    private static void sellStock(InventoryService inventory) {
        System.out.println("\n--- SELL STOCK ---");

        String id = readText("Enter product ID: ");
        Optional<Product> result = inventory.findProductById(id);

        if (result.isEmpty()) {
            System.out.println("Error: Product not found.");
            return;
        }

        Product product = result.get();
        System.out.println("Product: " + product.getName());
        System.out.println("Current Stock: " + product.getQuantity());
        System.out.printf("Current Price: Rs. %.2f%n", product.getPrice());

        int quantity = readInt("Enter sale quantity: ");
        if (quantity <= 0) {
            System.out.println("Error: Sale quantity must be positive.");
            return;
        }
        if (quantity > product.getQuantity()) {
            System.out.println("Error: Sale quantity exceeds available stock (" + product.getQuantity() + ").");
            return;
        }

        try {
            Transaction transaction = inventory.sellStock(id, quantity);
            displayReceipt(transaction, product.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Sale failed: " + e.getMessage());
        }
    }

    private static void displayReceipt(Transaction transaction, String productName) {
        System.out.println("\n==================================");
        System.out.println("       TRANSACTION RECEIPT        ");
        System.out.println("==================================");
        System.out.println("Transaction ID  : " + transaction.getTransactionId());
        System.out.println("Timestamp       : " + transaction.getTimestamp());
        System.out.println("Product ID      : " + transaction.getProductId());
        System.out.println("Product Name    : " + productName);
        System.out.println("Transaction Type: " + transaction.getType());
        System.out.println("Quantity        : " + transaction.getQuantity());
        System.out.printf("Unit Price      : Rs. %.2f%n", transaction.getUnitPrice());
        System.out.printf("Total Amount    : Rs. %.2f%n", transaction.getTotalAmount().doubleValue());
        System.out.println("==================================");
    }

    private static void viewTransactionHistory(InventoryService inventory) {
        List<Transaction> transactions = inventory.getTransactionHistory();
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n--- TRANSACTION HISTORY ---");
        for (Transaction txn : transactions) {
            System.out.println("Transaction ID  : " + txn.getTransactionId());
            System.out.println("Timestamp       : " + txn.getTimestamp());
            System.out.println("Product ID      : " + txn.getProductId());
            System.out.println("Transaction Type: " + txn.getType());
            System.out.println("Quantity        : " + txn.getQuantity());
            System.out.printf("Unit Price      : Rs. %.2f%n", txn.getUnitPrice());
            System.out.printf("Total Amount    : Rs. %.2f%n", txn.getTotalAmount().doubleValue());
            System.out.println("------------------------------");
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                double value = Double.parseDouble(
                        SCANNER.nextLine().trim());

                if (Double.isFinite(value) && value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Show the common validation message below.
            }

            System.out.println("Enter a valid positive price.");
        }
    }

    private static void showDashboard(InventoryService inventory) {
        InventoryDashboard dashboard = inventory.getInventoryDashboard();
        System.out.println(dashboard);
    }

    private static void showSalesReport(InventoryService inventory) {
        SalesAndPurchaseReport report = inventory.getSalesAndPurchaseReport();
        System.out.println(report);
    }

    private static void exportInventoryCsv(InventoryService inventory) {
        System.out.println("\n--- EXPORT INVENTORY TO CSV ---");
        System.out.print("Enter file name or path (press Enter for default 'reports/inventory.csv'): ");
        String input = SCANNER.nextLine().trim();
        Path path;
        if (input.isEmpty()) {
            path = Path.of("reports", "inventory.csv");
        } else {
            path = Path.of(input);
            if (path.getParent() == null) {
                path = Path.of("reports").resolve(path);
            }
        }

        try {
            Path exported = inventory.exportInventoryCsv(path);
            System.out.println("Inventory exported successfully to: " + exported);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void exportTransactionsCsv(InventoryService inventory) {
        System.out.println("\n--- EXPORT TRANSACTIONS TO CSV ---");
        System.out.print("Enter file name or path (press Enter for default 'reports/transactions.csv'): ");
        String input = SCANNER.nextLine().trim();
        Path path;
        if (input.isEmpty()) {
            path = Path.of("reports", "transactions.csv");
        } else {
            path = Path.of(input);
            if (path.getParent() == null) {
                path = Path.of("reports").resolve(path);
            }
        }

        try {
            Path exported = inventory.exportTransactionsCsv(path);
            System.out.println("Transactions exported successfully to: " + exported);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }
}
