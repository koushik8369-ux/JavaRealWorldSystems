package com.koushik.inventory;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.service.InventoryService;
import com.koushik.inventory.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InventoryPersistenceTest {

    private InventoryPersistenceTest() {
    }

    public static void main(String[] args) throws Exception {
        Path testDirectory = Files.createTempDirectory("inventory-tests-");
        try {
            testPersistenceAndInventoryOperations(testDirectory.resolve("products.txt"));
            testMalformedRecords(testDirectory.resolve("malformed.txt"));
            testFailedWritePreservesInventory(testDirectory.resolve("blocked"));
            testFailedUpdateAndDeletePreserveInventory(
                    testDirectory.resolve("replace-with-directory.txt"));
            System.out.println("All inventory persistence tests passed.");
        } finally {
            deleteTree(testDirectory);
        }
    }

    private static void testPersistenceAndInventoryOperations(Path file)
            throws IOException {
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

    private static void testFailedWritePreservesInventory(Path blockingPath)
            throws IOException {
        Files.writeString(blockingPath, "parent path is a file");
        InventoryService inventory =
                new InventoryService(new FileStorage(blockingPath.resolve("products.txt")));
        try {
            inventory.addProduct(new Product("P-1", "Cannot save", "Test",
                    1.0, 1, 0));
            throw new AssertionError("Failed write must be reported");
        } catch (java.io.UncheckedIOException e) {
            check(inventory.getAllProducts().isEmpty(),
                    "Failed add does not change in-memory inventory");
        }
    }

    private static void testFailedUpdateAndDeletePreserveInventory(Path file)
            throws IOException {
        FileStorage storage = new FileStorage(file);
        InventoryService inventory = new InventoryService(storage);
        inventory.addProduct(new Product("P-1", "Original", "Test", 3.0, 2, 0));

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("keep"), "block replacement");

        try {
            inventory.updateProduct("P-1", "Changed", "Test", 4.0, 3, 0);
            throw new AssertionError("Failed update write must be reported");
        } catch (java.io.UncheckedIOException e) {
            Product product = inventory.findProductById("P-1").orElseThrow();
            check(product.getName().equals("Original")
                            && product.getPrice() == 3.0
                            && product.getQuantity() == 2,
                    "Failed update does not change in-memory product values");
        }

        try {
            inventory.deleteProduct("P-1");
            throw new AssertionError("Failed delete write must be reported");
        } catch (java.io.UncheckedIOException e) {
            check(inventory.findProductById("P-1").isPresent(),
                    "Failed delete does not remove the in-memory product");
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
