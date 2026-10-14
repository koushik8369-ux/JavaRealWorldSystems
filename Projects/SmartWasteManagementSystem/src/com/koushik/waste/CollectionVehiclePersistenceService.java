

package com.koushik.waste;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionVehiclePersistenceService {

    private static final String HEADER =
            "vehicleId,registrationNumber,capacityKg";

    public void saveVehicles(
            List<CollectionVehicle> vehicles, Path file)
            throws IOException {

        if (vehicles == null || file == null) {
            throw new IllegalArgumentException(
                    "Vehicles and file path cannot be null.");
        }

        Set<String> ids = new HashSet<>();

        for (CollectionVehicle vehicle : vehicles) {
            if (vehicle == null) {
                throw new IllegalArgumentException(
                        "Vehicle list cannot contain null.");
            }

            if (!ids.add(vehicle.getVehicleId())) {
                throw new IllegalArgumentException(
                        "Duplicate vehicle ID: "
                                + vehicle.getVehicleId());
            }
        }

        Path absolutePath = file.toAbsolutePath();
        Path parent = absolutePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                absolutePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            writer.write(HEADER);
            writer.newLine();

            for (CollectionVehicle vehicle : vehicles) {
                writer.write(escape(vehicle.getVehicleId()));
                writer.write(",");
                writer.write(escape(vehicle.getRegistrationNumber()));
                writer.write(",");
                writer.write(Double.toString(vehicle.getCapacityKg()));
                writer.newLine();
            }
        }
    }

    public List<CollectionVehicle> loadVehicles(Path file)
            throws IOException {

        if (file == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null.");
        }

        if (!Files.exists(file)) {
            throw new IOException(
                    "Vehicle data file does not exist: " + file);
        }

        List<CollectionVehicle> vehicles = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> registrations = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            if (!HEADER.equals(reader.readLine())) {
                throw new IOException(
                        "Invalid vehicle CSV header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    List<String> fields = parseCsvLine(line);

                    if (fields.size() != 3) {
                        throw new IllegalArgumentException(
                                "Expected exactly 3 fields.");
                    }

                    String vehicleId = fields.get(0);
                    String registration = fields.get(1);
                    double capacity = Double.parseDouble(fields.get(2));

                    CollectionVehicle vehicle =
                            new CollectionVehicle(
                                    vehicleId, registration, capacity);

                    if (!ids.add(vehicleId)) {
                        throw new IllegalArgumentException(
                                "Duplicate vehicle ID: " + vehicleId);
                    }

                    if (!registrations.add(registration)) {
                        throw new IllegalArgumentException(
                                "Duplicate registration: " + registration);
                    }

                    vehicles.add(vehicle);
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                            "Invalid vehicle data at line "
                                    + lineNumber + ": "
                                    + exception.getMessage(),
                            exception);
                }
            }
        }

        return List.copyOf(vehicles);
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quoted CSV field.");
        }

        fields.add(field.toString());
        return fields;
    }
}
