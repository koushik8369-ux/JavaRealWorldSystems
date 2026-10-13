package com.koushik.inventory;

import com.koushik.inventory.model.InventoryDashboard;
import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.service.InventoryService;
import com.koushik.inventory.storage.FileStorage;
import com.koushik.inventory.util.CsvUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Phase 6 tests: Validation and Reliability edge cases, invariants, and failure resilience.
 */
public final class InventoryValidationReliabilityTest {

    private InventoryValidationReliabilityTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("inventory-phase6-tests-");
        try {
            runAll(tempDir);
            System.out.println("All Phase 6 validation and reliability tests passed successfully!");
        } finally {
            deleteTree(tempDir);
        }
    }

    public static void runAll(Path baseDir) throws Exception {
        Files.createDirectories(baseDir);
        testProductValidationAndEquality();
        testDashboardInvariants();
        testCsvUtilReliability();
        testServiceInputValidation(baseDir.resolve("service-validation-test"));
        testCsvExportPathEdgeCases(baseDir.resolve("export-paths-test"));
        testAtomicCsvExportOverwrites(baseDir.resolve("export-atomic-test"));
        testStorageTempFileCleanup(baseDir.resolve("temp-cleanup-test"));
    }

    private static void testProductValidationAndEquality() {
        // Required fields
        expectIllegalArgument(() -> new Product(null, "Name", "Cat", 10.0, 5, 2), "Null ID");
        expectIllegalArgument(() -> new Product("   ", "Name", "Cat", 10.0, 5, 2), "Blank ID");
        expectIllegalArgument(() -> new Product("P-1", null, "Cat", 10.0, 5, 2), "Null Name");
        expectIllegalArgument(() -> new Product("P-1", "  ", "Cat", 10.0, 5, 2), "Blank Name");
        expectIllegalArgument(() -> new Product("P-1", "Name", null, 10.0, 5, 2), "Null Category");
        expectIllegalArgument(() -> new Product("P-1", "Name", "  ", 10.0, 5, 2), "Blank Category");

        // Price validations
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", 0.0, 5, 2), "Zero price");
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", -1.0, 5, 2), "Negative price");
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", Double.NaN, 5, 2), "NaN price");
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", Double.POSITIVE_INFINITY, 5, 2), "Infinity price");

        // Quantity & threshold validations
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", 10.0, -1, 2), "Negative quantity");
        expectIllegalArgument(() -> new Product("P-1", "Name", "Cat", 10.0, 5, -1), "Negative threshold");

        // Equality & hashCode case-insensitivity
        Product p1 = new Product("ITEM-01", "Widget A", "Hardware", 19.99, 10, 2);
        Product p2 = new Product("item-01", "Widget B", "Other", 29.99, 5, 1);
        Product p3 = new Product("ITEM-02", "Widget A", "Hardware", 19.99, 10, 2);

        check(p1.equals(p2), "Product equals is case-insensitive on productId");
        check(p1.hashCode() == p2.hashCode(), "Product hashCode matches case-insensitively");
        check(!p1.equals(p3), "Different product IDs are not equal");
        check(!p1.equals(null), "Product equals null is false");
        check(!p1.equals("ITEM-01"), "Product equals non-Product is false");
    }

    private static void testDashboardInvariants() {
        // lowStockProductsCount cannot exceed totalProducts
        expectIllegalArgument(() -> new InventoryDashboard(2, 10, BigDecimal.TEN, 3),
                "Low stock products count cannot exceed total products");

        // Negative values rejected
        expectIllegalArgument(() -> new InventoryDashboard(-1, 10, BigDecimal.TEN, 0), "Negative total products");
        expectIllegalArgument(() -> new InventoryDashboard(5, -1, BigDecimal.TEN, 0), "Negative stock quantity");
        expectIllegalArgument(() -> new InventoryDashboard(5, 10, null, 0), "Null valuation");
        expectIllegalArgument(() -> new InventoryDashboard(5, 10, BigDecimal.valueOf(-5), 0), "Negative valuation");
        expectIllegalArgument(() -> new InventoryDashboard(5, 10, BigDecimal.TEN, -1), "Negative low-stock count");

        // Valid boundary
        InventoryDashboard valid = new InventoryDashboard(3, 15, BigDecimal.valueOf(100), 3);
        check(valid.getTotalProducts() == 3, "Valid total products");
        check(valid.getLowStockProductsCount() == 3, "All products can be low stock");
    }

    private static void testCsvUtilReliability() {
        // Non-finite doubles do not throw NumberFormatException
        check(CsvUtil.formatMonetary(Double.NaN).equals("0.00"), "NaN double formatted safely");
        check(CsvUtil.formatMonetary(Double.POSITIVE_INFINITY).equals("0.00"), "Infinity double formatted safely");
        check(CsvUtil.formatMonetary(Double.NEGATIVE_INFINITY).equals("0.00"), "Negative infinity double formatted safely");
        check(CsvUtil.formatMonetary((BigDecimal) null).equals("0.00"), "Null BigDecimal formatted safely");

        // Escaping
        check(CsvUtil.escape("").isEmpty(), "Empty field returns empty");
        check(CsvUtil.escape(null).isEmpty(), "Null field returns empty");
        check(CsvUtil.escape("Simple text").equals("Simple text"), "Plain text not quoted");
        check(CsvUtil.escape("With, comma").equals("\"With, comma\""), "Comma quoted");
        check(CsvUtil.escape("With \"quotes\"").equals("\"With \"\"quotes\"\"\""), "Quotes doubled");
        check(CsvUtil.escape("Line 1\nLine 2").equals("\"Line 1\nLine 2\""), "Newline quoted");
        check(CsvUtil.escape("Line 1\r\nLine 2").equals("\"Line 1\r\nLine 2\""), "CRLF quoted");

        // Rows
        check(CsvUtil.toCsvRow((List<String>) null).equals("\r\n"), "Null fields list returns empty row CRLF");
        check(CsvUtil.toCsvRow(List.of()).equals("\r\n"), "Empty fields list returns CRLF");
    }

    private static void testServiceInputValidation(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("P-VAL", "Validation Item", "General", 50.0, 10, 2));

        // findProductById with null / blank
        check(service.findProductById(null).isEmpty(), "findProductById(null) returns empty");
        check(service.findProductById("   ").isEmpty(), "findProductById(blank) returns empty");
        check(service.findProductById("P-val").isPresent(), "Case-insensitive lookup works");

        // findTransactionById with null / blank
        check(service.findTransactionById(null).isEmpty(), "findTransactionById(null) returns empty");
        check(service.findTransactionById("   ").isEmpty(), "findTransactionById(blank) returns empty");

        // deleteProduct with null / blank
        check(!service.deleteProduct(null), "deleteProduct(null) returns false");
        check(!service.deleteProduct("   "), "deleteProduct(blank) returns false");

        // updateProduct with invalid ID
        expectIllegalArgument(() -> service.updateProduct(null, "New", "Cat", 10.0, 5, 1),
                "updateProduct with null ID rejected");
        expectIllegalArgument(() -> service.updateProduct("   ", "New", "Cat", 10.0, 5, 1),
                "updateProduct with blank ID rejected");
        expectIllegalArgument(() -> service.updateProduct("NONEXISTENT", "New", "Cat", 10.0, 5, 1),
                "updateProduct with nonexistent ID rejected");

        // updateProduct with invalid values fails validation and leaves in-memory item untouched
        expectIllegalArgument(() -> service.updateProduct("P-VAL", "New", "Cat", -10.0, 5, 1),
                "Negative price in updateProduct rejected");
        expectIllegalArgument(() -> service.updateProduct("P-VAL", "New", "Cat", 10.0, -5, 1),
                "Negative quantity in updateProduct rejected");
        expectIllegalArgument(() -> service.updateProduct("P-VAL", "New", "Cat", 10.0, 5, -1),
                "Negative threshold in updateProduct rejected");

        Product unchanged = service.findProductById("P-VAL").orElseThrow();
        check(unchanged.getPrice() == 50.0 && unchanged.getQuantity() == 10,
                "In-memory product remained completely unchanged after failed updates");

        // purchaseStock validation and failure atomicity
        expectIllegalArgument(() -> service.purchaseStock(null, 5), "Purchase null ID");
        expectIllegalArgument(() -> service.purchaseStock("  ", 5), "Purchase blank ID");
        expectIllegalArgument(() -> service.purchaseStock("P-VAL", 0), "Purchase 0 quantity");
        expectIllegalArgument(() -> service.purchaseStock("P-VAL", -3), "Purchase negative quantity");
        expectIllegalArgument(() -> service.purchaseStock("UNKNOWN", 5), "Purchase unknown product");

        // sellStock validation and failure atomicity
        expectIllegalArgument(() -> service.sellStock(null, 5), "Sell null ID");
        expectIllegalArgument(() -> service.sellStock("  ", 5), "Sell blank ID");
        expectIllegalArgument(() -> service.sellStock("P-VAL", 0), "Sell 0 quantity");
        expectIllegalArgument(() -> service.sellStock("P-VAL", -2), "Sell negative quantity");
        expectIllegalArgument(() -> service.sellStock("UNKNOWN", 5), "Sell unknown product");
        expectIllegalArgument(() -> service.sellStock("P-VAL", 999), "Sell exceeding stock");

        check(service.getTransactionHistory().isEmpty(),
                "No transactions created after failed purchases and sales");
        check(service.findProductById("P-VAL").orElseThrow().getQuantity() == 10,
                "Stock quantity untouched after failed operations");
    }

    private static void testCsvExportPathEdgeCases(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path prodFile = dir.resolve("products.txt");
        Path txFile = dir.resolve("transactions.txt");
        FileStorage storage = new FileStorage(prodFile, txFile);
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("SAFE-ITEM", "Safe", "Cat", 10.0, 5, 1));

        // 1. Null target
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Null export path rejected");

        // 2. Blank target
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(Path.of("   "));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Blank export path rejected");

        // 3. Root directory path without filename (must throw IllegalArgumentException, not NPE)
        Path rootPath = Path.of(System.getProperty("user.dir")).getRoot();
        if (rootPath != null) {
            expectIllegalArgument(() -> {
                try {
                    service.exportInventoryCsv(rootPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "Root directory export path rejected with IllegalArgumentException");
        }

        // 4. Existing directory path
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Directory export path rejected");

        // 5. Active storage file names and paths
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(prodFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Overwriting active products.txt rejected");

        expectIllegalArgument(() -> {
            try {
                service.exportTransactionsCsv(txFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Overwriting active transactions.txt rejected");
    }

    private static void testAtomicCsvExportOverwrites(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("EX-1", "First Item", "Category", 15.0, 10, 2));

        Path targetCsv = dir.resolve("inventory_export.csv");

        // First export
        service.exportInventoryCsv(targetCsv);
        List<String> firstLines = Files.readAllLines(targetCsv);
        check(firstLines.size() == 2, "First export has 2 lines");

        // Add second product and re-export to SAME file
        service.addProduct(new Product("EX-2", "Second Item", "Category", 30.0, 5, 1));
        service.exportInventoryCsv(targetCsv);

        List<String> secondLines = Files.readAllLines(targetCsv);
        check(secondLines.size() == 3, "Re-exported CSV cleanly replaced the destination file with 3 lines");
        check(secondLines.get(1).contains("EX-1") && secondLines.get(2).contains("EX-2"),
                "Re-exported CSV contains both items without corruption");
    }

    private static void testStorageTempFileCleanup(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path prodFile = dir.resolve("products.txt");
        Path txFile = dir.resolve("transactions.txt");
        FileStorage storage = new FileStorage(prodFile, txFile);
        InventoryService service = new InventoryService(storage);

        // Perform multiple operations
        service.addProduct(new Product("CL-1", "Clean Item", "Cat", 10.0, 20, 2));
        service.purchaseStock("CL-1", 10);
        service.sellStock("CL-1", 5);

        // Verify no orphaned .tmp files in storage directory
        try (var stream = Files.list(dir)) {
            List<Path> tmpFiles = stream.filter(p -> p.getFileName().toString().endsWith(".tmp")).toList();
            check(tmpFiles.isEmpty(), "No orphaned .tmp files left in storage directory after persistence");
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
