
package com.koushik.waste;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WasteBinPersistenceServiceTest {

    private static int checks = 0;

    private static final WasteBinPersistenceService SERVICE =
            new WasteBinPersistenceService();

    public static void main(String[] args) throws IOException {
        Path directory = Files.createTempDirectory("waste-tests");

        try {
            testSaveAndLoad(directory);
            testSpecialCharacters(directory);
            testMissingFile(directory);
            testInvalidHeader(directory);
            testInvalidWasteData(directory);
            testDuplicateIds(directory);
            testNullArguments();
        } finally {
            deleteDirectory(directory);
        }

        System.out.println(
                "\nAll Phase 6 persistence tests passed: " + checks);
    }

    private static void testSaveAndLoad(Path directory)
            throws IOException {
        Path file = directory.resolve("bins.csv");

        WasteBin first = new WasteBin(
                "B1", "Market", 100.0, 75.0);
        WasteBin second = new WasteBin(
                "B2", "City Park", 200.0, 120.0);

        SERVICE.saveBins(List.of(first, second), file);
        List<WasteBin> loaded = SERVICE.loadBins(file);

        check(loaded.size() == 2, "Save and load multiple bins");
        check(loaded.get(0).getBinId().equals("B1"),
                "Preserve first bin ID");
        check(loaded.get(1).getLocation().equals("City Park"),
                "Preserve location");
        check(closeTo(loaded.get(0).getCapacityKg(), 100.0),
                "Preserve bin capacity");
        check(closeTo(loaded.get(1).getCurrentWasteKg(), 120.0),
                "Preserve current waste");
    }

    private static void testSpecialCharacters(Path directory)
            throws IOException {
        Path file = directory.resolve("special.csv");

        WasteBin bin = new WasteBin(
                "B3", "Market, \"North\"", 100.0, 25.0);

        SERVICE.saveBins(List.of(bin), file);
        List<WasteBin> loaded = SERVICE.loadBins(file);

        check(loaded.size() == 1,
                "Load bin with special characters");
        check(loaded.get(0).getLocation().equals(
                        "Market, \"North\""),
                "Preserve commas and quotes in location");
    }

    private static void testMissingFile(Path directory) {
        Path file = directory.resolve("missing.csv");

        expectIOException(
                () -> SERVICE.loadBins(file),
                "Reject missing file");
    }

    private static void testInvalidHeader(Path directory)
            throws IOException {
        Path file = directory.resolve("bad-header.csv");

        Files.writeString(
                file,
                "wrong,header\nB1,Market,100,50\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadBins(file),
                "Reject invalid CSV header");
    }

    private static void testInvalidWasteData(Path directory)
            throws IOException {
        Path file = directory.resolve("invalid-data.csv");

        Files.writeString(
                file,
                "binId,location,capacityKg,currentWasteKg\n"
                        + "B1,Market,100,150\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadBins(file),
                "Reject waste above bin capacity");
    }

    private static void testDuplicateIds(Path directory)
            throws IOException {
        Path file = directory.resolve("duplicate.csv");

        Files.writeString(
                file,
                "binId,location,capacityKg,currentWasteKg\n"
                        + "B1,Market,100,20\n"
                        + "B1,Park,200,30\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadBins(file),
                "Reject duplicate IDs when loading");

        expectIllegalArgument(
                () -> saveBins(List.of(
                        new WasteBin("B1", "Market", 100, 20),
                        new WasteBin("B1", "Park", 200, 30)), file),
                "Reject duplicate IDs when saving");
    }

    private static void testNullArguments() {
        expectIllegalArgument(
                () -> saveBins(null, Path.of("bins.csv")),
                "Reject null bin list");

        expectIllegalArgument(
                () -> saveBins(List.of(), null),
                "Reject null save path");

        expectIllegalArgument(
                () -> loadBins(null),
                "Reject null load path");

        expectIllegalArgument(
                () -> saveBins(
                        Arrays.asList((WasteBin) null),
                        Path.of("bins.csv")),
                "Reject null bin in list");
    }

    private static void saveBins(List<WasteBin> bins, Path path) {
        try {
            SERVICE.saveBins(bins, path);
        } catch (IOException exception) {
            throw new AssertionError(
                    "Unexpected I/O error in save test", exception);
        }
    }

    private static List<WasteBin> loadBins(Path path) {
        try {
            return SERVICE.loadBins(path);
        } catch (IOException exception) {
            throw new AssertionError(
                    "Unexpected I/O error in load test", exception);
        }
    }

    private static void expectIOException(
            IoAction action, String description) {
        checks++;

        try {
            action.run();
            throw new AssertionError("FAILED: " + description);
        } catch (IOException expected) {
            System.out.println("PASSED: " + description);
        }
    }

    private static void expectIllegalArgument(
            Runnable action, String description) {
        checks++;

        try {
            action.run();
            throw new AssertionError("FAILED: " + description);
        } catch (IllegalArgumentException expected) {
            System.out.println("PASSED: " + description);
        }
    }

    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < 0.0001;
    }

    private static void check(boolean condition, String description) {
        checks++;

        if (!condition) {
            throw new AssertionError("FAILED: " + description);
        }

        System.out.println("PASSED: " + description);
    }

    private static void deleteDirectory(Path directory)
            throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(
                    Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }
}
