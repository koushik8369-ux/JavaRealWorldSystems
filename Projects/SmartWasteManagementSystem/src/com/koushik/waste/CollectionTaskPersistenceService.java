
package com.koushik.waste;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionTaskPersistenceService {

    private static final String HEADER =
            "taskId,binId,vehicleId,status";

    public record TaskRecord(
            String taskId,
            String binId,
            String vehicleId,
            CollectionTask.Status status) {

        public TaskRecord {
            if (taskId == null || taskId.isBlank()
                    || binId == null || binId.isBlank()
                    || status == null) {
                throw new IllegalArgumentException(
                        "Task ID, bin ID, and status are required.");
            }

            if (status == CollectionTask.Status.IN_PROGRESS
                    && (vehicleId == null || vehicleId.isBlank())) {
                throw new IllegalArgumentException(
                        "An in-progress task requires a vehicle ID.");
            }

            if (status != CollectionTask.Status.IN_PROGRESS
                    && vehicleId != null && !vehicleId.isBlank()) {
                throw new IllegalArgumentException(
                        "Only in-progress tasks may retain a vehicle assignment.");
            }
        }
    }

    public void saveTasks(List<TaskRecord> records, Path file)
            throws IOException {

        if (records == null || file == null) {
            throw new IllegalArgumentException(
                    "Task records and file path cannot be null.");
        }

        Set<String> taskIds = new HashSet<>();

        for (TaskRecord record : records) {
            if (record == null) {
                throw new IllegalArgumentException(
                        "Task records cannot contain null.");
            }

            if (!taskIds.add(record.taskId())) {
                throw new IllegalArgumentException(
                        "Duplicate task ID: " + record.taskId());
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

            for (TaskRecord record : records) {
                writer.write(escape(record.taskId()));
                writer.write(",");
                writer.write(escape(record.binId()));
                writer.write(",");
                writer.write(escape(
                        record.vehicleId() == null
                                ? "" : record.vehicleId()));
                writer.write(",");
                writer.write(record.status().name());
                writer.newLine();
            }
        }
    }

    public List<TaskRecord> loadTasks(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null.");
        }

        if (!Files.exists(file)) {
            throw new IOException(
                    "Task data file does not exist: " + file);
        }

        List<TaskRecord> records = new ArrayList<>();
        Set<String> taskIds = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            if (!HEADER.equals(reader.readLine())) {
                throw new IOException("Invalid task CSV header.");
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

                    if (fields.size() != 4) {
                        throw new IllegalArgumentException(
                                "Expected exactly 4 fields.");
                    }

                    String vehicleId = fields.get(2).isBlank()
                            ? null : fields.get(2);

                    TaskRecord record = new TaskRecord(
                            fields.get(0),
                            fields.get(1),
                            vehicleId,
                            CollectionTask.Status.valueOf(fields.get(3)));

                    if (!taskIds.add(record.taskId())) {
                        throw new IllegalArgumentException(
                                "Duplicate task ID: " + record.taskId());
                    }

                    records.add(record);
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                            "Invalid task data at line "
                                    + lineNumber + ": "
                                    + exception.getMessage(),
                            exception);
                }
            }
        }

        return List.copyOf(records);
    }

    /**
     * Restores saved tasks after bins and vehicles have been loaded.
     *
     * All records are validated before any task is restored, so common
     * data errors do not leave the service partially restored.
     */
    public void restoreTasks(
            Path file,
            WasteManagementService wasteManagementService,
            CollectionOperationsService operationsService)
            throws IOException {

        if (wasteManagementService == null
                || operationsService == null) {
            throw new IllegalArgumentException(
                    "Waste management and operations services cannot be null.");
        }

        List<TaskRecord> records = loadTasks(file);

        Map<String, CollectionVehicle> vehicles = new HashMap<>();

        for (CollectionVehicle vehicle : operationsService.getAllVehicles()) {
            vehicles.put(vehicle.getVehicleId(), vehicle);
        }

        Set<String> taskIds = new HashSet<>();

        for (CollectionTask existing : operationsService.getAllTasks()) {
            taskIds.add(existing.getTaskId());
        }

        Set<String> vehiclesBeingAssigned = new HashSet<>();

        // Validate every record before changing the operations service.
        for (TaskRecord record : records) {
            if (!taskIds.add(record.taskId())) {
                throw new IllegalArgumentException(
                        "Task already exists: " + record.taskId());
            }

            WasteBin bin = wasteManagementService.findBin(record.binId());

            if (record.status() != CollectionTask.Status.IN_PROGRESS) {
                continue;
            }

            CollectionVehicle vehicle = vehicles.get(record.vehicleId());

            if (vehicle == null) {
                throw new IllegalArgumentException(
                        "Vehicle not found: " + record.vehicleId());
            }

            if (!vehicle.isAvailable()) {
                throw new IllegalStateException(
                        "Vehicle is already assigned: "
                                + record.vehicleId());
            }

            if (!vehiclesBeingAssigned.add(record.vehicleId())) {
                throw new IllegalArgumentException(
                        "Vehicle assigned to multiple in-progress tasks: "
                                + record.vehicleId());
            }

            if (bin.getCurrentWasteKg() > vehicle.getCapacityKg()) {
                throw new IllegalStateException(
                        "Vehicle capacity is insufficient for task: "
                                + record.taskId());
            }
        }

        // All records are valid; restore them into the live service.
        for (TaskRecord record : records) {
            WasteBin bin =
                    wasteManagementService.findBin(record.binId());

            operationsService.restoreTask(
                    record.taskId(),
                    bin,
                    record.vehicleId(),
                    record.status());
        }
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
