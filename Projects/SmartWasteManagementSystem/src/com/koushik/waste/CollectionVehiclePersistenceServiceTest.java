
package com.koushik.waste;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CollectionVehiclePersistenceServiceTest {

    private static int checks = 0;

    private static final CollectionVehiclePersistenceService SERVICE =
            new CollectionVehiclePersistenceService();

    public static void main(String[] args) throws IOException {
        Path directory =
                Files.createTempDirectory("vehicle-persistence-tests");

        try {
            testSaveAndLoad(directory);
            testSpecialCharacters(directory);
            testMissingFile(directory);
            testInvalidHeader(directory);
            testInvalidCapacity(directory);
            testDuplicateIds(directory);
            testDuplicateRegistrations(directory);
            testNullArguments();
        } finally {
            deleteDirectory(directory);
        }

        System.out.println(
                "\nAll vehicle persistence tests passed: " + checks);
    }

    private static void testSaveAndLoad(Path directory)
            throws IOException {
        Path file = directory.resolve("vehicles.csv");

        CollectionVehicle first =
                new CollectionVehicle("V1", "KA01AA1111", 150.0);
        CollectionVehicle second =
                new CollectionVehicle("V2", "KA02BB2222", 250.0);

        SERVICE.saveVehicles(List.of(first, second), file);
        List<CollectionVehicle> loaded = SERVICE.loadVehicles(file);

        check(loaded.size() == 2, "Save and load multiple vehicles");
        check(loaded.get(0).getVehicleId().equals("V1"),
                "Preserve vehicle ID");
        check(loaded.get(1).getRegistrationNumber().equals("KA02BB2222"),
                "Preserve registration number");
        check(closeTo(loaded.get(0).getCapacityKg(), 150.0),
                "Preserve vehicle capacity");
        check(loaded.get(0).isAvailable(),
                "Loaded vehicle starts available");
    }

    private static void testSpecialCharacters(Path directory)
            throws IOException {
        Path file = directory.resolve("special-vehicles.csv");

        CollectionVehicle vehicle =
                new CollectionVehicle("V3", "KA01, \"NORTH\"", 100.0);

        SERVICE.saveVehicles(List.of(vehicle), file);
        List<CollectionVehicle> loaded = SERVICE.loadVehicles(file);

        check(loaded.size() == 1,
                "Load vehicle with special characters");
        check(loaded.get(0).getRegistrationNumber().equals(
                        "KA01, \"NORTH\""),
                "Preserve commas and quotes in registration");
    }

    private static void testMissingFile(Path directory) {
        expectIOException(
                () -> SERVICE.loadVehicles(
                        directory.resolve("missing.csv")),
                "Reject missing vehicle file");
    }

    private static void testInvalidHeader(Path directory)
            throws IOException {
        Path file = directory.resolve("bad-header.csv");

        Files.writeString(
                file,
                "wrong,header\nV1,KA01AA1111,100\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadVehicles(file),
                "Reject invalid vehicle CSV header");
    }

    private static void testInvalidCapacity(Path directory)
            throws IOException {
        Path file = directory.resolve("bad-capacity.csv");

        Files.writeString(
                file,
                "vehicleId,registrationNumber,capacityKg\n"
                        + "V1,KA01AA1111,-20\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadVehicles(file),
                "Reject invalid vehicle capacity");
    }

    private static void testDuplicateIds(Path directory)
            throws IOException {
        Path file = directory.resolve("duplicate-ids.csv");

        Files.writeString(
                file,
                "vehicleId,registrationNumber,capacityKg\n"
                        + "V1,KA01AA1111,100\n"
                        + "V1,KA02BB2222,200\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadVehicles(file),
                "Reject duplicate vehicle IDs");

        expectIllegalArgument(
                () -> saveVehicles(
                        List.of(
                                new CollectionVehicle(
                                        "V1", "KA01AA1111", 100),
                                new CollectionVehicle(
                                        "V1", "KA02BB2222", 200)),
                        file),
                "Reject duplicate IDs when saving");
    }

    private static void testDuplicateRegistrations(Path directory)
            throws IOException {
        Path file = directory.resolve("duplicate-registrations.csv");

        Files.writeString(
                file,
                "vehicleId,registrationNumber,capacityKg\n"
                        + "V1,KA01AA1111,100\n"
                        + "V2,KA01AA1111,200\n",
                StandardCharsets.UTF_8);

        expectIOException(
                () -> SERVICE.loadVehicles(file),
                "Reject duplicate registration numbers");
    }

    private static void testNullArguments() {
        expectIllegalArgument(
                () -> saveVehicles(null, Path.of("vehicles.csv")),
                "Reject null vehicle list");

        expectIllegalArgument(
                () -> saveVehicles(List.of(), null),
                "Reject null save path");

        expectIllegalArgument(
                () -> loadVehicles(null),
                "Reject null load path");

        expectIllegalArgument(
                () -> saveVehicles(
                        Arrays.asList((CollectionVehicle) null),
                        Path.of("vehicles.csv")),
                "Reject null vehicle in list");
    }

    private static void saveVehicles(
            List<CollectionVehicle> vehicles, Path path) {
        try {
            SERVICE.saveVehicles(vehicles, path);
        } catch (IOException exception) {
            throw new AssertionError(
                    "Unexpected I/O error in save test", exception);
        }
    }

    private static List<CollectionVehicle> loadVehicles(Path path) {
        try {
            return SERVICE.loadVehicles(path);
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
