
package com.koushik.waste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RoutePlanner {

    private final Map<String, Map<String, Double>> graph =
            new LinkedHashMap<>();

    public record Route(List<String> locations, double distanceKm) {
        public Route {
            locations = List.copyOf(locations);
        }
    }

    private record NodeDistance(String location, double distance) {}

    public void addLocation(String location) {
        validateLocationName(location);
        graph.putIfAbsent(location, new LinkedHashMap<>());
    }

    public void addRoad(
            String from, String to, double distanceKm) {

        validateLocationName(from);
        validateLocationName(to);

        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException(
                    "Road distance must be finite and positive.");
        }

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "A road cannot connect a location to itself.");
        }

        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            throw new IllegalArgumentException(
                    "Both locations must be registered first.");
        }

        graph.get(from).put(to, distanceKm);
        graph.get(to).put(from, distanceKm);
    }

    public Route findShortestRoute(String start, String destination) {
        validateLocationName(start);
        validateLocationName(destination);

        if (!graph.containsKey(start) || !graph.containsKey(destination)) {
            throw new IllegalArgumentException(
                    "Start and destination must be registered.");
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        for (String location : graph.keySet()) {
            distances.put(location, Double.POSITIVE_INFINITY);
        }

        distances.put(start, 0.0);

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(
                Comparator.comparingDouble(NodeDistance::distance));

        queue.add(new NodeDistance(start, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            double knownDistance = distances.get(current.location());

            if (current.distance() > knownDistance) {
                continue;
            }

            if (current.location().equals(destination)) {
                break;
            }

            for (Map.Entry<String, Double> edge :
                    graph.get(current.location()).entrySet()) {

                String neighbor = edge.getKey();
                double roadDistance = edge.getValue();
                double candidate = knownDistance + roadDistance;

                if (candidate < distances.get(neighbor)) {
                    distances.put(neighbor, candidate);
                    previous.put(neighbor, current.location());

                    queue.add(new NodeDistance(neighbor, candidate));
                }
            }
        }

        double totalDistance = distances.get(destination);

        if (!Double.isFinite(totalDistance)) {
            throw new IllegalArgumentException(
                    "No route exists between " + start
                            + " and " + destination + ".");
        }

        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);

            if (current.equals(start)) {
                break;
            }

            current = previous.get(current);
        }

        Collections.reverse(path);

        return new Route(path, totalDistance);
    }

    public int getLocationCount() {
        return graph.size();
    }

    private void validateLocationName(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location name cannot be empty.");
        }
    }
}
