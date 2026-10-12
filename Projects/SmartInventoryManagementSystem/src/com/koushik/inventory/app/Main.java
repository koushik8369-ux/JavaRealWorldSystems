
package com.koushik.inventory.app;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.service.InventoryService;

import java.io.UncheckedIOException;
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
                    case 9, 0 -> {
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
                9. Exit
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
}
