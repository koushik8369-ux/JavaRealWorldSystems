package com.koushik.inventory.storage;

import com.koushik.inventory.model.Product;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores one product per line as six tab-separated fields:
 * Base64 UTF-8 product ID, name, and category, followed by price,
 * quantity, and low-stock threshold.
 */
public class FileStorage {

    private static final Path DEFAULT_PATH = Path.of("data", "products.txt");

    private final Path file;

    public FileStorage() {
        this(DEFAULT_PATH);
    }

    public FileStorage(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("Storage file path is required.");
        }
        this.file = file;
    }

    public List<Product> load() throws IOException {
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
        if (products == null) {
            throw new IllegalArgumentException("Products cannot be null.");
        }

        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "products-", ".tmp");

        try {
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

            Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, absoluteFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, absoluteFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
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
}
