package com.koushik.inventory.storage;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.model.Transaction;
import com.koushik.inventory.model.TransactionType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Persists products and transactions using UTF-8 tab-separated text files.
 *
 * Product format:
 * Base64 product ID, Base64 name, Base64 category, price, quantity, low-stock threshold.
 *
 * Transaction format:
 * Base64 transaction ID, Base64 product ID, type (PURCHASE/SALE), quantity, unit price, total amount, ISO-8601 timestamp.
 */
public class FileStorage {

    private static final Path DEFAULT_PATH = Path.of("data", "products.txt");
    private static final Path DEFAULT_TRANSACTION_PATH = Path.of("data", "transactions.txt");

    private final Path file;
    private final Path transactionFile;

    public FileStorage() {
        this(DEFAULT_PATH, DEFAULT_TRANSACTION_PATH);
    }

    public FileStorage(Path file) {
        this(file, deriveTransactionPath(file));
    }

    public FileStorage(Path file, Path transactionFile) {
        if (file == null) {
            throw new IllegalArgumentException("Storage file path is required.");
        }
        if (transactionFile == null) {
            throw new IllegalArgumentException("Transaction storage file path is required.");
        }
        this.file = file;
        this.transactionFile = transactionFile;
    }

    private static Path deriveTransactionPath(Path productFile) {
        if (productFile == null) {
            throw new IllegalArgumentException("Storage file path is required.");
        }
        Path parent = productFile.getParent();
        return parent != null
                ? parent.resolve("transactions.txt")
                : Path.of("transactions.txt");
    }

    public Path getFile() {
        return file;
    }

    public Path getTransactionFile() {
        return transactionFile;
    }

    public List<Product> load() throws IOException {
        return loadProducts();
    }

    public List<Product> loadProducts() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Product> products = new ArrayList<>(lines.size());
        Set<String> productIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String[] fields = lines.get(index).split("\t", -1);
            if (fields.length != 6) {
                throw malformedRecord(lineNumber,
                        "expected exactly six tab-separated fields");
            }

            Product product;
            try {
                String productId = decodeText(fields[0]);
                String name = decodeText(fields[1]);
                String category = decodeText(fields[2]);
                product = new Product(
                        productId,
                        name,
                        category,
                        Double.parseDouble(fields[3]),
                        Integer.parseInt(fields[4]),
                        Integer.parseInt(fields[5]));
            } catch (IllegalArgumentException | CharacterCodingException e) {
                throw malformedRecord(lineNumber, e.getMessage(), e);
            }
            if (!productIds.add(product.getProductId())) {
                throw malformedRecord(lineNumber,
                        "duplicate product ID '" + product.getProductId() + "'");
            }
            products.add(product);
        }

        return List.copyOf(products);
    }

    public void save(List<Product> products) throws IOException {
        saveProducts(products);
    }

    public void saveProducts(List<Product> products) throws IOException {
        if (products == null) {
            throw new IllegalArgumentException("Products cannot be null.");
        }

        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, "products-", ".tmp");

        try {
            String contents = serializeProducts(products);
            Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8);
            moveAtomicOrReplace(temporaryFile, absoluteFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public List<Transaction> loadTransactions() throws IOException {
        if (!Files.exists(transactionFile)) {
            return List.of();
        }

        List<String> lines = Files.readAllLines(transactionFile, StandardCharsets.UTF_8);
        List<Transaction> transactions = new ArrayList<>(lines.size());
        Set<String> transactionIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String[] fields = lines.get(index).split("\t", -1);
            if (fields.length != 7) {
                throw malformedTransactionRecord(lineNumber,
                        "expected exactly seven tab-separated fields");
            }

            Transaction transaction;
            try {
                String transactionId = decodeText(fields[0]);
                String productId = decodeText(fields[1]);
                TransactionType type = TransactionType.valueOf(fields[2]);
                int quantity = Integer.parseInt(fields[3]);
                double unitPrice = Double.parseDouble(fields[4]);
                BigDecimal totalAmount = new BigDecimal(fields[5]);
                LocalDateTime timestamp = LocalDateTime.parse(fields[6]);

                transaction = new Transaction(
                        transactionId,
                        productId,
                        type,
                        quantity,
                        unitPrice,
                        totalAmount,
                        timestamp);
            } catch (IllegalArgumentException | CharacterCodingException | DateTimeParseException e) {
                throw malformedTransactionRecord(lineNumber, e.getMessage(), e);
            }

            if (!transactionIds.add(transaction.getTransactionId())) {
                throw malformedTransactionRecord(lineNumber,
                        "duplicate transaction ID '" + transaction.getTransactionId() + "'");
            }
            transactions.add(transaction);
        }

        return List.copyOf(transactions);
    }

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null.");
        }

        Path absoluteFile = transactionFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, "transactions-", ".tmp");

        try {
            String contents = serializeTransactions(transactions);
            Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8);
            moveAtomicOrReplace(temporaryFile, absoluteFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Persists products and transactions sequentially with best-effort rollback recovery.
     *
     * <p><strong>Atomicity Note:</strong> Individual file replacements use temporary files
     * and atomic moves where supported by the operating system and filesystem. However,
     * because products and transactions are stored in separate files, true cross-file
     * ACID atomicity is not guaranteed without a transactional filesystem or unified journal.
     * If product replacement fails, a best-effort rollback restores the transaction file to
     * its prior content.</p>
     *
     * @param products products to persist
     * @param transactions transactions to persist
     * @throws IOException if persisting fails
     */
    public void saveAll(List<Product> products, List<Transaction> transactions) throws IOException {
        if (products == null) {
            throw new IllegalArgumentException("Products cannot be null.");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null.");
        }

        Path absProduct = file.toAbsolutePath();
        Path absTransaction = transactionFile.toAbsolutePath();

        Path productParent = absProduct.getParent();
        if (productParent != null) {
            Files.createDirectories(productParent);
        }
        Path transactionParent = absTransaction.getParent();
        if (transactionParent != null) {
            Files.createDirectories(transactionParent);
        }

        String productContents = serializeProducts(products);
        String transactionContents = serializeTransactions(transactions);

        boolean transactionExisted = Files.exists(absTransaction);
        byte[] previousTransactionBytes = transactionExisted
                ? Files.readAllBytes(absTransaction)
                : null;

        Path tempProduct = null;
        Path tempTransaction = null;

        try {
            tempProduct = Files.createTempFile(productParent, "products-", ".tmp");
            tempTransaction = Files.createTempFile(transactionParent, "transactions-", ".tmp");

            Files.writeString(tempProduct, productContents, StandardCharsets.UTF_8);
            Files.writeString(tempTransaction, transactionContents, StandardCharsets.UTF_8);

            // Replace transaction file first
            try {
                moveAtomicOrReplace(tempTransaction, absTransaction);
            } catch (IOException e) {
                // Transaction file failed to replace; product file was never touched
                throw e;
            }

            // Now replace product file
            try {
                moveAtomicOrReplace(tempProduct, absProduct);
            } catch (IOException productMoveError) {
                // Product replace failed! Roll back transaction file to prior state
                try {
                    if (transactionExisted) {
                        Files.write(absTransaction, previousTransactionBytes);
                    } else {
                        Files.deleteIfExists(absTransaction);
                    }
                } catch (IOException rollbackError) {
                    productMoveError.addSuppressed(rollbackError);
                }
                throw productMoveError;
            }
        } finally {
            if (tempProduct != null) {
                Files.deleteIfExists(tempProduct);
            }
            if (tempTransaction != null) {
                Files.deleteIfExists(tempTransaction);
            }
        }
    }

    private String serializeProducts(List<Product> products) {
        StringBuilder contents = new StringBuilder();
        Set<String> productIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Product product : products) {
            if (product == null) {
                throw new IllegalArgumentException(
                        "Inventory cannot contain a null product.");
            }
            if (!productIds.add(product.getProductId())) {
                throw new IllegalArgumentException(
                        "Duplicate product ID cannot be saved: "
                                + product.getProductId());
            }
            contents.append(encodeText(product.getProductId())).append('\t')
                    .append(encodeText(product.getName())).append('\t')
                    .append(encodeText(product.getCategory())).append('\t')
                    .append(Double.toString(product.getPrice())).append('\t')
                    .append(product.getQuantity()).append('\t')
                    .append(product.getLowStockThreshold()).append('\n');
        }
        return contents.toString();
    }

    private String serializeTransactions(List<Transaction> transactions) {
        StringBuilder contents = new StringBuilder();
        Set<String> transactionIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Transaction txn : transactions) {
            if (txn == null) {
                throw new IllegalArgumentException(
                        "Transaction history cannot contain a null transaction.");
            }
            if (!transactionIds.add(txn.getTransactionId())) {
                throw new IllegalArgumentException(
                        "Duplicate transaction ID cannot be saved: "
                                + txn.getTransactionId());
            }
            contents.append(encodeText(txn.getTransactionId())).append('\t')
                    .append(encodeText(txn.getProductId())).append('\t')
                    .append(txn.getType().name()).append('\t')
                    .append(txn.getQuantity()).append('\t')
                    .append(Double.toString(txn.getUnitPrice())).append('\t')
                    .append(txn.getTotalAmount().toPlainString()).append('\t')
                    .append(txn.getTimestamp().toString()).append('\n');
        }
        return contents.toString();
    }

    private static void moveAtomicOrReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String encodeText(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeText(String value)
            throws CharacterCodingException {
        byte[] decoded = Base64.getDecoder().decode(value);
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(decoded))
                .toString();
    }

    private static IOException malformedRecord(
            int lineNumber, String reason) {
        return malformedRecord(lineNumber, reason, null);
    }

    private static IOException malformedRecord(
            int lineNumber, String reason, Exception cause) {
        String message = "Malformed product record at line " + lineNumber
                + ": " + (reason == null ? "invalid field value" : reason);
        return cause == null
                ? new IOException(message)
                : new IOException(message, cause);
    }

    private static IOException malformedTransactionRecord(
            int lineNumber, String reason) {
        return malformedTransactionRecord(lineNumber, reason, null);
    }

    private static IOException malformedTransactionRecord(
            int lineNumber, String reason, Exception cause) {
        String message = "Malformed transaction record at line " + lineNumber
                + ": " + (reason == null ? "invalid field value" : reason);
        return cause == null
                ? new IOException(message)
                : new IOException(message, cause);
    }
}
