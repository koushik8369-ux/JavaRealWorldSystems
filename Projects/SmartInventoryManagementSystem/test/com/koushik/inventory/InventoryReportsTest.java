package com.koushik.inventory;

import com.koushik.inventory.model.BestSellingProduct;
import com.koushik.inventory.model.InventoryDashboard;
import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.SalesAndPurchaseReport;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.model.TransactionType;
import com.koushik.inventory.service.InventoryService;
import com.koushik.inventory.storage.FileStorage;
import com.koushik.inventory.util.CsvUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 5 tests for Inventory Dashboard, Sales & Purchase Analytics, and CSV Export.
 */
public final class InventoryReportsTest {

    private InventoryReportsTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("inventory-phase5-tests-");
        try {
            runAll(tempDir);
            System.out.println("All Phase 5 reports and analytics tests passed successfully!");
        } finally {
            deleteTree(tempDir);
        }
    }

    public static void runAll(Path baseDir) throws Exception {
        Files.createDirectories(baseDir);
        testEmptyInventoryAndTransactions(baseDir.resolve("empty-test"));
        testDashboardCalculations(baseDir.resolve("dashboard-test"));
        testSalesAndPurchaseReports(baseDir.resolve("reports-test"));
        testHistoricalPriceIntegrity(baseDir.resolve("history-price-test"));
        testBestSellingWithDeletedProduct(baseDir.resolve("deleted-bestseller-test"));
        testCsvHeadersAndContent(baseDir.resolve("csv-headers-test"));
        testCsvEscapingRfc4180(baseDir.resolve("csv-escaping-test"));
        testExportErrorsAndStorageProtection(baseDir.resolve("export-errors-test"));
    }

    private static void testEmptyInventoryAndTransactions(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        // 1. Dashboard on empty inventory
        InventoryDashboard dashboard = service.getInventoryDashboard();
        check(dashboard.getTotalProducts() == 0, "Empty inventory has 0 products");
        check(dashboard.getTotalStockQuantity() == 0, "Empty inventory has 0 stock quantity");
        check(dashboard.getTotalInventoryValue().compareTo(BigDecimal.ZERO) == 0, "Empty inventory value is 0");
        check(dashboard.getLowStockProductsCount() == 0, "Empty inventory has 0 low stock products");
        check(dashboard.toString().contains("Total Products       : 0"), "Dashboard string formats empty inventory");

        // 2. Sales report on empty transactions
        SalesAndPurchaseReport report = service.getSalesAndPurchaseReport();
        check(report.getSalesTransactionCount() == 0, "Empty sales transaction count is 0");
        check(report.getPurchaseTransactionCount() == 0, "Empty purchase transaction count is 0");
        check(report.getTotalUnitsSold() == 0, "Empty units sold is 0");
        check(report.getTotalUnitsPurchased() == 0, "Empty units purchased is 0");
        check(report.getTotalSalesValue().compareTo(BigDecimal.ZERO) == 0, "Empty sales value is 0");
        check(report.getTotalPurchaseValue().compareTo(BigDecimal.ZERO) == 0, "Empty purchase value is 0");
        check(report.getBestSellingProduct().isEmpty(), "No best-selling product when no sales");
        check(report.toString().contains("None (no sales recorded)"), "Report string handles no sales");

        // 3. Export empty inventory to CSV
        Path invCsv = service.exportInventoryCsv(dir.resolve("empty_inventory.csv"));
        check(Files.exists(invCsv), "Empty inventory CSV is created");
        List<String> invLines = Files.readAllLines(invCsv, StandardCharsets.UTF_8);
        check(invLines.size() == 1, "Empty inventory CSV contains only header line");
        check(invLines.getFirst().startsWith("Product ID,Name,Category"), "Inventory CSV header is correct");

        // 4. Export empty transactions to CSV
        Path txCsv = service.exportTransactionsCsv(dir.resolve("empty_transactions.csv"));
        check(Files.exists(txCsv), "Empty transaction CSV is created");
        List<String> txLines = Files.readAllLines(txCsv, StandardCharsets.UTF_8);
        check(txLines.size() == 1, "Empty transaction CSV contains only header line");
        check(txLines.getFirst().startsWith("Transaction ID,Timestamp,Product ID"), "Transaction CSV header is correct");
    }

    private static void testDashboardCalculations(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("P-1", "Standard Mouse", "Peripherals", 25.50, 10, 3));
        service.addProduct(new Product("P-2", "Low Stock Keyboard", "Peripherals", 80.00, 2, 5));
        service.addProduct(new Product("P-3", "Zero Stock Cable", "Accessories", 5.00, 0, 1));

        InventoryDashboard dashboard = service.getInventoryDashboard();
        check(dashboard.getTotalProducts() == 3, "Total products is 3");
        check(dashboard.getTotalStockQuantity() == 12, "Total stock quantity is 10 + 2 + 0 = 12");
        // 10*25.50 = 255.00, 2*80 = 160.00, 0*5 = 0.00 -> Total = 415.00
        check(dashboard.getTotalInventoryValue().compareTo(new BigDecimal("415.00")) == 0,
                "Total inventory value is 415.00");
        // Low stock: P-2 (2 <= 5) and P-3 (0 <= 1) -> count is 2
        check(dashboard.getLowStockProductsCount() == 2, "Low stock product count is 2");

        // Update a product and re-verify dashboard
        service.updateProduct("P-2", "Low Stock Keyboard", "Peripherals", 80.00, 10, 5);
        InventoryDashboard updatedDash = service.getInventoryDashboard();
        check(updatedDash.getTotalStockQuantity() == 20, "Total stock quantity is updated to 20");
        check(updatedDash.getLowStockProductsCount() == 1, "Low stock count drops to 1 after restock");

        // Delete product
        service.deleteProduct("P-3");
        InventoryDashboard afterDelete = service.getInventoryDashboard();
        check(afterDelete.getTotalProducts() == 2, "Total products decreases after deletion");
        check(afterDelete.getLowStockProductsCount() == 0, "Low stock count is 0 after removing zero-stock item");
    }

    private static void testSalesAndPurchaseReports(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("PROD-A", "Alpha Gadget", "Gadgets", 50.00, 20, 2));
        service.addProduct(new Product("PROD-B", "Beta Widget", "Widgets", 30.00, 15, 2));

        LocalDateTime baseTime = LocalDateTime.of(2026, 2, 1, 9, 0);

        // Purchases
        service.purchaseStock("PROD-A", 10, baseTime.plusHours(1)); // 10 * 50 = 500.00
        service.purchaseStock("PROD-B", 5, baseTime.plusHours(2));  // 5 * 30 = 150.00

        // Sales
        service.sellStock("PROD-A", 4, baseTime.plusHours(3)); // 4 * 50 = 200.00
        service.sellStock("PROD-B", 8, baseTime.plusHours(4)); // 8 * 30 = 240.00
        service.sellStock("PROD-B", 3, baseTime.plusHours(5)); // 3 * 30 = 90.00

        SalesAndPurchaseReport report = service.getSalesAndPurchaseReport();

        check(report.getPurchaseTransactionCount() == 2, "Purchase transaction count is 2");
        check(report.getTotalUnitsPurchased() == 15, "Total units purchased is 15");
        check(report.getTotalPurchaseValue().compareTo(new BigDecimal("650.00")) == 0,
                "Total purchase value is 650.00 (500 + 150)");

        check(report.getSalesTransactionCount() == 3, "Sales transaction count is 3");
        check(report.getTotalUnitsSold() == 15, "Total units sold is 4 + 8 + 3 = 15");
        check(report.getTotalSalesValue().compareTo(new BigDecimal("530.00")) == 0,
                "Total sales value is 530.00 (200 + 240 + 90)");

        // Best seller: PROD-B sold 11 units, PROD-A sold 4 units -> PROD-B wins
        check(report.getBestSellingProduct().isPresent(), "Best-selling product is present");
        BestSellingProduct bestSeller = report.getBestSellingProduct().get();
        check(bestSeller.getProductId().equalsIgnoreCase("PROD-B"), "Best-selling product is PROD-B");
        check(bestSeller.getProductName().equals("Beta Widget"), "Best-selling product name is Beta Widget");
        check(bestSeller.getUnitsSold() == 11, "Best-selling units sold is 11");
        check(bestSeller.getTotalSalesRevenue().compareTo(new BigDecimal("330.00")) == 0,
                "Best-selling product revenue is 330.00");
    }

    private static void testHistoricalPriceIntegrity(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("P-HIST", "Original Widget", "Category", 10.00, 50, 5));

        // Sell 10 units at Rs. 10.00 -> 100.00
        service.sellStock("P-HIST", 10);

        SalesAndPurchaseReport initialReport = service.getSalesAndPurchaseReport();
        check(initialReport.getTotalSalesValue().compareTo(new BigDecimal("100.00")) == 0,
                "Initial sales total is 100.00");

        // Now update product price to Rs. 999.00
        service.updateProduct("P-HIST", "Original Widget", "Category", 999.00, 40, 5);

        // Sales totals must STILL reflect historical transaction price (100.00), not the updated price
        SalesAndPurchaseReport reportAfterPriceChange = service.getSalesAndPurchaseReport();
        check(reportAfterPriceChange.getTotalSalesValue().compareTo(new BigDecimal("100.00")) == 0,
                "Sales total preserves historical transaction price after product price update");
        BestSellingProduct bsp = reportAfterPriceChange.getBestSellingProduct().orElseThrow();
        check(bsp.getTotalSalesRevenue().compareTo(new BigDecimal("100.00")) == 0,
                "Best selling revenue preserves historical transaction price");
    }

    private static void testBestSellingWithDeletedProduct(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("P-TEMP", "Temporary Item", "General", 20.00, 30, 2));
        service.sellStock("P-TEMP", 15);

        // Now delete the product from active inventory
        check(service.deleteProduct("P-TEMP"), "Delete product succeeds");

        SalesAndPurchaseReport report = service.getSalesAndPurchaseReport();
        check(report.getBestSellingProduct().isPresent(), "Best seller still identified from transaction history");
        BestSellingProduct bsp = report.getBestSellingProduct().get();
        check(bsp.getProductId().equalsIgnoreCase("P-TEMP"), "Product ID is P-TEMP");
        check(bsp.getUnitsSold() == 15, "Units sold is 15");
        check(bsp.getProductName().contains("Unknown") || bsp.getProductName().contains("catalog"),
                "Product name indicates product is not in catalog / deleted");
    }

    private static void testCsvHeadersAndContent(Path dir) throws IOException {
        Files.createDirectories(dir);
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("INV-01", "Gaming Mouse", "Peripherals", 49.99, 10, 2));
        service.purchaseStock("INV-01", 5);
        service.sellStock("INV-01", 3);

        Path invCsv = dir.resolve("inventory_export.csv");
        Path txCsv = dir.resolve("transactions_export.csv");

        service.exportInventoryCsv(invCsv);
        service.exportTransactionsCsv(txCsv);

        List<String> invLines = Files.readAllLines(invCsv, StandardCharsets.UTF_8);
        check(invLines.size() == 2, "Inventory CSV has 1 header + 1 data line");
        check(invLines.get(0).equals("Product ID,Name,Category,Price,Quantity,Low Stock Threshold,Low Stock Alert,Stock Value"),
                "Inventory CSV headers match specification");
        check(invLines.get(1).contains("INV-01") && invLines.get(1).contains("49.99"),
                "Inventory CSV data line contains product details");

        List<String> txLines = Files.readAllLines(txCsv, StandardCharsets.UTF_8);
        check(txLines.size() == 3, "Transaction CSV has 1 header + 2 transaction lines");
        check(txLines.get(0).equals("Transaction ID,Timestamp,Product ID,Transaction Type,Quantity,Unit Price,Total Amount"),
                "Transaction CSV headers match specification");
        check(txLines.get(1).contains("PURCHASE") && txLines.get(1).contains("INV-01"),
                "First transaction line contains PURCHASE details");
        check(txLines.get(2).contains("SALE") && txLines.get(2).contains("INV-01"),
                "Second transaction line contains SALE details");
    }

    private static void testCsvEscapingRfc4180(Path dir) throws IOException {
        Files.createDirectories(dir);

        // Unit test CsvUtil directly
        check(CsvUtil.escape("Simple").equals("Simple"), "Simple field not escaped");
        check(CsvUtil.escape("Widget, Deluxe").equals("\"Widget, Deluxe\""), "Comma escaped with quotes");
        check(CsvUtil.escape("15\" Monitor").equals("\"15\"\" Monitor\""), "Double quotes doubled inside quotes");
        check(CsvUtil.escape("Line 1\nLine 2").equals("\"Line 1\nLine 2\""), "Newline escaped with quotes");
        check(CsvUtil.escape("Line 1\r\nLine 2").equals("\"Line 1\r\nLine 2\""), "CRLF escaped with quotes");
        check(CsvUtil.escape(null).isEmpty(), "Null escaped to empty string");

        // End-to-end service test with complex product characters
        FileStorage storage = new FileStorage(dir.resolve("products.txt"), dir.resolve("transactions.txt"));
        InventoryService service = new InventoryService(storage);

        String trickyName = "Pro Gaming, 27\" Screen\nDeluxe";
        String trickyCategory = "Monitors & \"Displays\", Special";
        service.addProduct(new Product("TRICKY-1", trickyName, trickyCategory, 299.95, 5, 1));

        Path exportFile = dir.resolve("tricky_export.csv");
        service.exportInventoryCsv(exportFile);

        String fullCsv = Files.readString(exportFile, StandardCharsets.UTF_8);
        check(fullCsv.contains("\"Pro Gaming, 27\"\" Screen\nDeluxe\""),
                "Tricky product name escaped correctly per RFC 4180");
        check(fullCsv.contains("\"Monitors & \"\"Displays\"\", Special\""),
                "Tricky category escaped correctly per RFC 4180");
    }

    private static void testExportErrorsAndStorageProtection(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path prodFile = dir.resolve("products.txt");
        Path txFile = dir.resolve("transactions.txt");
        FileStorage storage = new FileStorage(prodFile, txFile);
        InventoryService service = new InventoryService(storage);

        service.addProduct(new Product("SAFE-1", "Safe Item", "General", 10.0, 5, 1));

        // 1. Target cannot overwrite product storage file
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(prodFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Export cannot overwrite active products.txt");

        // 2. Target cannot overwrite transaction storage file
        expectIllegalArgument(() -> {
            try {
                service.exportTransactionsCsv(txFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Export cannot overwrite active transactions.txt");

        // 3. Target cannot be named products.txt or transactions.txt
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(dir.resolve("products.txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Export target named products.txt is forbidden");

        expectIllegalArgument(() -> {
            try {
                service.exportTransactionsCsv(dir.resolve("transactions.txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Export target named transactions.txt is forbidden");

        // 4. Target cannot be null
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Target cannot be null");

        // 5. Target cannot be an existing directory
        expectIllegalArgument(() -> {
            try {
                service.exportInventoryCsv(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Target cannot be a directory");

        // Verify storage files were not altered
        check(Files.exists(prodFile) && storage.load().size() == 1
                && storage.load().getFirst().getProductId().equals("SAFE-1"),
                "Product storage file remains completely intact and readable");
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
