package com.koushik.inventory;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.model.TransactionType;
import com.koushik.inventory.service.InventoryService;
import com.koushik.inventory.storage.FileStorage;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public final class InventoryPersistenceTest {

    private InventoryPersistenceTest() {
    }

    public static void main(String[] args) throws Exception {
        Path testDirectory = Files.createTempDirectory("inventory-tests-");
        try {
            // Case 13: Existing product management functionality
            testExistingInventoryOperations(testDirectory.resolve("existing-products.txt"));

            // Cases 1-11: Purchase, sale, financial totals, restart persistence, ordering, price isolation, validation
            testPurchaseAndSaleOperations(testDirectory.resolve("transactions-test"));

            // Case 12: Simulated persistence failures and consistency rollback
            testSimulatedPersistenceFailures(testDirectory.resolve("failures-test"));

            // Case 14: Malformed transaction records and duplicate IDs
            testMalformedRecords(testDirectory.resolve("malformed-products.txt"));
            testMalformedTransactionRecords(testDirectory.resolve("malformed-tx.txt"));

            System.out.println("All 14 test requirements passed successfully!");
        } finally {
            deleteTree(testDirectory);
        }
    }

    /**
     * Case 13: Existing product-management functionality still works.
     */
    private static void testExistingInventoryOperations(Path file) throws IOException {
        FileStorage storage = new FileStorage(file);
        InventoryService inventory = new InventoryService(storage);
        check(inventory.getAllProducts().isEmpty(), "Missing file starts empty");

        Product first = new Product("P-1", "Café & tea\tset",
                "Home / kitchen", 10.5, 4, 2);
        inventory.addProduct(first);
        check(Files.exists(file), "Adding creates the file");

        InventoryService restarted = new InventoryService(storage);
        Product restored = restarted.findProductById("p-1").orElseThrow();
        check(restored.getName().equals(first.getName()), "Name round-trips");
        check(restored.getCategory().equals(first.getCategory()),
                "Category round-trips");
        check(restored.getPrice() == 10.5 && restored.getQuantity() == 4
                        && restored.getLowStockThreshold() == 2,
                "Numeric fields round-trip");

        restarted.addProduct(new Product("P-2", "Special #2", "Office goods",
                2.0, 1, 1));
        check(restarted.getAllProducts().size() == 2, "Both products remain");
        expectIllegalArgument(() -> restarted.addProduct(
                new Product("p-1", "Duplicate", "Test", 1, 0, 0)),
                "Duplicate IDs are case-insensitive");

        expectIllegalArgument(() -> new Product("bad", "Bad", "Test", -1, 0, 0),
                "Negative prices are rejected");
        expectIllegalArgument(() -> new Product("bad", "Bad", "Test", 1, -1, 0),
                "Negative quantities are rejected");

        restarted.updateProduct("P-1", "Renamed product", "New category",
                12.25, 5, 3);
        InventoryService afterUpdate = new InventoryService(storage);
        Product updated = afterUpdate.findProductById("P-1").orElseThrow();
        check(updated.getName().equals("Renamed product")
                        && updated.getCategory().equals("New category")
                        && updated.getPrice() == 12.25
                        && updated.getQuantity() == 5
                        && updated.getLowStockThreshold() == 3,
                "Updated values survive restart");
        check(afterUpdate.getProductsSortedByPrice().getFirst().getProductId()
                        .equals("P-2"),
                "Price sorting works");
        check(afterUpdate.getLowStockProducts().stream()
                        .anyMatch(product -> product.getProductId().equals("P-2")),
                "Low-stock filtering works");
        check(afterUpdate.getTotalInventoryValue() == 63.25,
                "Inventory valuation works");
        check(afterUpdate.findProductById("p-2").isPresent(),
                "Case-insensitive search works");

        check(afterUpdate.deleteProduct("P-2"), "Delete succeeds");
        InventoryService afterDelete = new InventoryService(storage);
        check(afterDelete.findProductById("P-2").isEmpty(),
                "Deleted product stays deleted after restart");

        String savedContents = Files.readString(file);
        expectIllegalArgument(
                () -> afterDelete.updateProduct("P-1", "Invalid", "Category",
                        Double.NaN, 5, 3),
                "Invalid update values are rejected");
        check(Files.readString(file).equals(savedContents),
                "Invalid update leaves the file unchanged");
    }

    /**
     * Cases 1 through 11:
     * 1. Purchase increases stock correctly.
     * 2. Sale decreases stock correctly.
     * 3. A sale exceeding available stock is rejected.
     * 4. A nonexistent product cannot be purchased or sold.
     * 5. Zero and negative quantities are rejected.
     * 6. Transaction totals are calculated correctly.
     * 7. Purchase and sale records survive an application restart.
     * 8. Product inventory survives an application restart.
     * 9. Transaction history is returned in chronological order.
     * 10. Updating a product price does not alter historical transaction prices.
     * 11. Failed validation does not create a transaction.
     */
    private static void testPurchaseAndSaleOperations(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path prodFile = dir.resolve("products.txt");
        Path txFile = dir.resolve("transactions.txt");
        FileStorage storage = new FileStorage(prodFile, txFile);
        InventoryService service = new InventoryService(storage);

        // Set up initial products
        service.addProduct(new Product("P-100", "Wireless Mouse", "Electronics", 25.50, 10, 3));
        service.addProduct(new Product("P-200", "Mechanical Keyboard", "Electronics", 75.00, 5, 2));

        check(service.getTransactionHistory().isEmpty(), "Initial transaction history is empty");

        // 1. Purchase increases stock correctly.
        LocalDateTime time1 = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        Transaction purchase1 = service.purchaseStock("P-100", 5, time1);
        check(service.findProductById("P-100").orElseThrow().getQuantity() == 15,
                "Case 1: Purchase increases stock correctly (10 + 5 = 15)");
        check(purchase1.getType() == TransactionType.PURCHASE, "Transaction type is PURCHASE");
        check(purchase1.getQuantity() == 5, "Transaction quantity is 5");
        check(purchase1.getUnitPrice() == 25.50, "Transaction unit price matches product price");

        // 2. Sale decreases stock correctly.
        LocalDateTime time2 = LocalDateTime.of(2026, 1, 1, 11, 0, 0);
        Transaction sale1 = service.sellStock("P-100", 4, time2);
        check(service.findProductById("P-100").orElseThrow().getQuantity() == 11,
                "Case 2: Sale decreases stock correctly (15 - 4 = 11)");
        check(sale1.getType() == TransactionType.SALE, "Transaction type is SALE");
        check(sale1.getQuantity() == 4, "Transaction quantity is 4");

        // 3. A sale exceeding available stock is rejected.
        int currentStock = service.findProductById("P-100").orElseThrow().getQuantity();
        expectIllegalArgument(() -> service.sellStock("P-100", currentStock + 1),
                "Case 3: Sale exceeding available stock is rejected");
        check(service.findProductById("P-100").orElseThrow().getQuantity() == currentStock,
                "Case 3: Stock unchanged after rejected sale");

        // 4. A nonexistent product cannot be purchased or sold.
        expectIllegalArgument(() -> service.purchaseStock("NONEXISTENT", 2),
                "Case 4: Purchase nonexistent product is rejected");
        expectIllegalArgument(() -> service.sellStock("NONEXISTENT", 2),
                "Case 4: Sale nonexistent product is rejected");

        // 5. Zero and negative quantities are rejected.
        expectIllegalArgument(() -> service.purchaseStock("P-100", 0),
                "Case 5: Zero purchase quantity rejected");
        expectIllegalArgument(() -> service.purchaseStock("P-100", -3),
                "Case 5: Negative purchase quantity rejected");
        expectIllegalArgument(() -> service.sellStock("P-100", 0),
                "Case 5: Zero sale quantity rejected");
        expectIllegalArgument(() -> service.sellStock("P-100", -2),
                "Case 5: Negative sale quantity rejected");
        check(service.findProductById("P-100").orElseThrow().getQuantity() == 11,
                "Case 5: Stock unchanged after negative/zero quantity attempts");

        // Integer overflow protection on purchase
        service.addProduct(new Product("P-MAX", "Overflow Test", "Test", 1.0, Integer.MAX_VALUE - 5, 0));
        expectIllegalArgument(() -> service.purchaseStock("P-MAX", 10),
                "Integer overflow on purchase is rejected");
        check(service.findProductById("P-MAX").orElseThrow().getQuantity() == Integer.MAX_VALUE - 5,
                "Stock unchanged after overflow attempt");

        // 6. Transaction totals are calculated correctly.
        check(purchase1.getTotalAmount().compareTo(BigDecimal.valueOf(127.50)) == 0,
                "Case 6: Purchase total correctly calculated (5 * 25.50 = 127.50)");
        check(sale1.getTotalAmount().compareTo(BigDecimal.valueOf(102.00)) == 0,
                "Case 6: Sale total correctly calculated (4 * 25.50 = 102.00)");

        // 11. Failed validation does not create a transaction.
        int txCountBefore = service.getTransactionHistory().size();
        expectIllegalArgument(() -> service.sellStock("P-100", 999), "Exceeding stock");
        expectIllegalArgument(() -> service.purchaseStock("P-100", -1), "Negative quantity");
        expectIllegalArgument(() -> service.purchaseStock("BAD-ID", 5), "Bad ID");
        check(service.getTransactionHistory().size() == txCountBefore,
                "Case 11: Failed validations do not create transactions");

        // 10. Updating a product price does not alter historical transaction prices.
        service.updateProduct("P-100", "Wireless Mouse v2", "Electronics", 99.99, 11, 3);
        check(service.findProductById("P-100").orElseThrow().getPrice() == 99.99,
                "Product price updated to 99.99");
        Transaction histPurchase = service.findTransactionById(purchase1.getTransactionId()).orElseThrow();
        check(histPurchase.getUnitPrice() == 25.50,
                "Case 10: Historical purchase unit price remains unchanged at 25.50");
        check(histPurchase.getTotalAmount().compareTo(BigDecimal.valueOf(127.50)) == 0,
                "Case 10: Historical purchase total amount remains unchanged at 127.50");
        Transaction histSale = service.findTransactionById(sale1.getTransactionId()).orElseThrow();
        check(histSale.getUnitPrice() == 25.50,
                "Case 10: Historical sale unit price remains unchanged at 25.50");

        // Perform another purchase at the new price
        LocalDateTime time3 = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        Transaction purchase2 = service.purchaseStock("P-100", 2, time3);
        check(purchase2.getUnitPrice() == 99.99,
                "New transaction uses updated product price (99.99)");
        check(purchase2.getTotalAmount().compareTo(BigDecimal.valueOf(199.98)) == 0,
                "New transaction total uses updated price (2 * 99.99 = 199.98)");

        // 7. Purchase and sale records survive an application restart.
        // 8. Product inventory survives an application restart.
        InventoryService restarted = new InventoryService(storage);
        Product restoredP100 = restarted.findProductById("P-100").orElseThrow();
        check(restoredP100.getQuantity() == 13,
                "Case 8: Product inventory survives restart (11 + 2 = 13)");
        check(restoredP100.getPrice() == 99.99,
                "Case 8: Updated price survives restart");

        List<Transaction> restartedHistory = restarted.getTransactionHistory();
        check(restartedHistory.size() == 3,
                "Case 7: All 3 transactions survived application restart");
        check(restarted.findTransactionById(purchase1.getTransactionId()).isPresent(),
                "Case 7: First purchase exists after restart");
        check(restarted.findTransactionById(sale1.getTransactionId()).isPresent(),
                "Case 7: Sale exists after restart");
        check(restarted.findTransactionById(purchase2.getTransactionId()).isPresent(),
                "Case 7: Second purchase exists after restart");

        // 9. Transaction history is returned in chronological order.
        for (int i = 0; i < restartedHistory.size() - 1; i++) {
            Transaction current = restartedHistory.get(i);
            Transaction next = restartedHistory.get(i + 1);
            check(!current.getTimestamp().isAfter(next.getTimestamp()),
                    "Case 9: History is returned in chronological order");
        }

        // Verify newly added transaction after restart gets a unique subsequent ID
        Transaction postRestartTx = restarted.purchaseStock("P-200", 1);
        check(!postRestartTx.getTransactionId().equalsIgnoreCase(purchase1.getTransactionId()),
                "Post-restart transaction ID is unique");
        check(!postRestartTx.getTransactionId().equalsIgnoreCase(purchase2.getTransactionId()),
                "Post-restart transaction ID is unique");
    }

    /**
     * Case 12: Simulated persistence failures do not leave inventory and transaction history inconsistent.
     */
    private static void testSimulatedPersistenceFailures(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path prodFile = dir.resolve("products.txt");
        Path txFile = dir.resolve("transactions.txt");
        FileStorage storage = new FileStorage(prodFile, txFile);
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("P-SAFE", "Safe Item", "General", 50.0, 10, 2));
        check(service.findProductById("P-SAFE").orElseThrow().getQuantity() == 10, "Initial qty is 10");
        check(service.getTransactionHistory().isEmpty(), "Initial tx history is empty");

        String initialProdFileContent = Files.readString(prodFile);

        // Subcase A: Simulate failure when product file replacement fails (e.g. product file path is a directory)
        // We delete product file and replace it with a non-empty directory so move/replace fails
        Files.delete(prodFile);
        Files.createDirectory(prodFile);
        Files.writeString(prodFile.resolve("blocker"), "blocks replacement");

        try {
            service.purchaseStock("P-SAFE", 5);
            throw new AssertionError("Simulated product persistence failure must throw exception");
        } catch (java.io.UncheckedIOException e) {
            // Verify in-memory state remained consistent
            check(service.findProductById("P-SAFE").orElseThrow().getQuantity() == 10,
                    "Case 12A: In-memory product quantity was NOT modified after failed purchase");
            check(service.getTransactionHistory().isEmpty(),
                    "Case 12A: In-memory transaction was NOT added after failed purchase");
            // Verify transaction file was rolled back / deleted
            check(!Files.exists(txFile) || Files.readString(txFile).isEmpty(),
                    "Case 12A: Transaction file was rolled back and did not record the failed transaction");
        }

        // Clean up blocker and restore product file
        Files.delete(prodFile.resolve("blocker"));
        Files.delete(prodFile);
        Files.writeString(prodFile, initialProdFileContent);

        // Reload clean service
        service = new InventoryService(storage);
        check(service.findProductById("P-SAFE").orElseThrow().getQuantity() == 10, "Restored qty is 10");

        // Subcase B: Simulate failure when transaction file replacement fails
        Files.createDirectory(txFile);
        Files.writeString(txFile.resolve("blocker"), "blocks replacement");

        try {
            service.sellStock("P-SAFE", 3);
            throw new AssertionError("Simulated transaction persistence failure must throw exception");
        } catch (java.io.UncheckedIOException e) {
            // Verify in-memory state remained consistent
            check(service.findProductById("P-SAFE").orElseThrow().getQuantity() == 10,
                    "Case 12B: In-memory product quantity was NOT modified after failed sale");
            check(service.getTransactionHistory().isEmpty(),
                    "Case 12B: In-memory transaction was NOT added after failed sale");
            // Verify product file was NOT modified
            check(Files.readString(prodFile).equals(initialProdFileContent),
                    "Case 12B: Product file remained completely untouched after failed sale");
        }

        // Clean up blocker
        Files.delete(txFile.resolve("blocker"));
        Files.delete(txFile);
    }

    /**
     * Case 14: Malformed transaction records and duplicate IDs are reported safely.
     */
    private static void testMalformedTransactionRecords(Path file) throws IOException {
        // Record with wrong field count (6 instead of 7)
        Files.writeString(file, "VFgtMQ==\tUA==\tPURCHASE\t1\t10.0\t10.0\n");
        try {
            new FileStorage(file.resolveSibling("products.txt"), file).loadTransactions();
            throw new AssertionError("Expected malformed transaction record error for 6 fields");
        } catch (IOException e) {
            check(e.getMessage().contains("line 1") && e.getMessage().contains("seven tab-separated"),
                    "Case 14: Reports line number and 7 tab-separated fields requirement");
        }

        // Record with invalid enum type
        Files.writeString(file, "VFgtMQ==\tUA==\tINVALID_TYPE\t1\t10.0\t10.0\t2026-01-01T00:00:00\n");
        try {
            new FileStorage(file.resolveSibling("products.txt"), file).loadTransactions();
            throw new AssertionError("Expected malformed transaction record error for invalid enum");
        } catch (IOException e) {
            check(e.getMessage().contains("line 1"), "Case 14: Reports line 1 for invalid enum");
        }

        // Record with negative quantity
        Files.writeString(file, "VFgtMQ==\tUA==\tPURCHASE\t-5\t10.0\t10.0\t2026-01-01T00:00:00\n");
        try {
            new FileStorage(file.resolveSibling("products.txt"), file).loadTransactions();
            throw new AssertionError("Expected error for negative quantity in transaction");
        } catch (IOException e) {
            check(e.getMessage().contains("line 1"), "Case 14: Reports line 1 for negative quantity");
        }

        // Duplicate transaction IDs
        Files.writeString(file,
                "VFgtMDAwMQ==\tUA==\tPURCHASE\t1\t10.0\t10.0\t2026-01-01T00:00:00\n"
                + "VFgtMDAwMQ==\tUA==\tSALE\t1\t10.0\t10.0\t2026-01-01T01:00:00\n");
        try {
            new FileStorage(file.resolveSibling("products.txt"), file).loadTransactions();
            throw new AssertionError("Expected duplicate transaction ID error");
        } catch (IOException e) {
            check(e.getMessage().contains("duplicate transaction ID"),
                    "Case 14: Reports duplicate transaction ID safely");
        }
    }

    private static void testMalformedRecords(Path file) throws IOException {
        Files.writeString(file, "not-a-valid-record\n");
        try {
            new InventoryService(new FileStorage(file));
            throw new AssertionError("Malformed data must be reported");
        } catch (java.io.UncheckedIOException e) {
            check(e.getMessage().contains("line 1")
                            && e.getMessage().contains("six tab-separated"),
                    "Malformed record reports its line and reason");
        }

        Files.writeString(file, "UEQtMQ==\tQQ==\tQg==\t1.0\t0\t0\n"
                + "cGQtMQ==\tQw==\tRA==\t1.0\t0\t0\n");
        try {
            new FileStorage(file).load();
            throw new AssertionError("Duplicate IDs in file must be reported");
        } catch (IOException e) {
            check(e.getMessage().contains("duplicate product ID"),
                    "Duplicate persisted ID reports a useful error");
        }
    }

    private static void expectIllegalArgument(Runnable operation, String message) {
        try {
            operation.run();
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (Files.exists(root)) {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
