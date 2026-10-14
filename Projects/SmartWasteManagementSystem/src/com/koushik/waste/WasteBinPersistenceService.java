
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

public class WasteBinPersistenceService {

    private static final String HEADER =
            "binId,location,capacityKg,currentWasteKg";

    public void saveBins(List<WasteBin> bins, Path file)
            throws IOException {

        if (bins == null || file == null) {
            throw new IllegalArgumentException(
                    "Bins and file path cannot be null.");
        }

        Set<String> ids = new HashSet<>();

        for (WasteBin bin : bins) {
            if (bin == null) {
                throw new IllegalArgumentException(
                        "Bin list cannot contain null.");
            }

            if (!ids.add(bin.getBinId())) {
                throw new IllegalArgumentException(
                        "Duplicate bin ID: " + bin.getBinId());
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

            for (WasteBin bin : bins) {
                writer.write(escape(bin.getBinId()));
                writer.write(",");
                writer.write(escape(bin.getLocation()));
                writer.write(",");
                writer.write(Double.toString(bin.getCapacityKg()));
                writer.write(",");
                writer.write(Double.toString(bin.getCurrentWasteKg()));
                writer.newLine();
            }
        }
    }

    public List<WasteBin> loadBins(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null.");
        }

        if (!Files.exists(file)) {
            throw new IOException(
                    "Waste-bin data file does not exist: " + file);
        }

        List<WasteBin> bins = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException(
                        "Invalid waste-bin CSV header.");
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

                    String binId = fields.get(0);
                    String location = fields.get(1);
                    double capacity = Double.parseDouble(fields.get(2));
                    double currentWaste =
                            Double.parseDouble(fields.get(3));

                    WasteBin bin = new WasteBin(
                            binId, location, capacity, currentWaste);

                    if (!ids.add(binId)) {
                        throw new IllegalArgumentException(
                                "Duplicate bin ID: " + binId);
                    }

                    bins.add(bin);
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                            "Invalid waste-bin data at line "
                                    + lineNumber + ": "
                                    + exception.getMessage(),
                            exception);
                }
            }
        }

        return List.copyOf(bins);
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
