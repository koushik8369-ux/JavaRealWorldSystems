
package com.koushik.waste;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WasteManagementService {

    private final Map<String, WasteBin> bins = new LinkedHashMap<>();

    public void registerBin(WasteBin bin) {
        if (bin == null) {
            throw new IllegalArgumentException(
                    "Waste bin cannot be null.");
        }

        if (bins.containsKey(bin.getBinId())) {
            throw new IllegalArgumentException(
                    "Duplicate bin ID: " + bin.getBinId());
        }

        bins.put(bin.getBinId(), bin);
    }

    public WasteBin findBin(String binId) {
        if (binId == null || binId.isBlank()) {
            throw new IllegalArgumentException(
                    "Bin ID cannot be empty.");
        }

        WasteBin bin = bins.get(binId);

        if (bin == null) {
            throw new IllegalArgumentException(
                    "Bin not found: " + binId);
        }

        return bin;
    }

    public boolean removeBin(String binId) {
        if (binId == null || binId.isBlank()) {
            throw new IllegalArgumentException(
                    "Bin ID cannot be empty.");
        }

        return bins.remove(binId) != null;
    }

    public List<WasteBin> getAllBins() {
        return List.copyOf(bins.values());
    }

    public List<WasteBin> getBinsNeedingCollection() {
        List<WasteBin> result = new ArrayList<>();

        for (WasteBin bin : bins.values()) {
            if (bin.needsCollection()) {
                result.add(bin);
            }
        }

        result.sort(
                Comparator.comparingDouble(
                        WasteBin::getFillPercentage).reversed());

        return List.copyOf(result);
    }

    public int getBinCount() {
        return bins.size();
    }
}
