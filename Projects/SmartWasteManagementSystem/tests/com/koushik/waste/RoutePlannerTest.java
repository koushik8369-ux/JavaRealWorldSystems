
package com.koushik.waste;

import java.util.List;

public class RoutePlannerTest {

    private static int passed = 0;

    public static void main(String[] args) {
        testShortestRoute();
        testDirectRoute();
        testSameStartAndDestination();
        testUnreachableDestination();
        testUnknownLocation();
        testInvalidDistance();
        testMissingRoadEndpoint();
        testLocationCount();
        testRouteIsImmutable();

        System.out.println();
        System.out.println("All route planner tests passed: " + passed);
    }

    private static RoutePlanner createNetwork() {
        RoutePlanner planner = new RoutePlanner();

        planner.addLocation("Depot");
        planner.addLocation("Market");
        planner.addLocation("Main Road");
        planner.addLocation("City Park");
        planner.addLocation("Hospital");
        planner.addLocation("Remote Area");

        planner.addRoad("Depot", "Market", 4.0);
        planner.addRoad("Depot", "Main Road", 10.0);
        planner.addRoad("Market", "Main Road", 2.0);
        planner.addRoad("Market", "City Park", 5.0);
        planner.addRoad("Main Road", "City Park", 1.0);
        planner.addRoad("City Park", "Hospital", 3.0);

        return planner;
    }

    private static void testShortestRoute() {
        RoutePlanner planner = createNetwork();

        RoutePlanner.Route route =
                planner.findShortestRoute("Depot", "Hospital");

        check(route.locations().equals(
                        List.of("Depot", "Market",
                                "Main Road", "City Park", "Hospital")),
                "Find shortest path through multiple locations");

        check(Double.compare(route.distanceKm(), 10.0) == 0,
                "Calculate total shortest distance");
    }

    private static void testDirectRoute() {
        RoutePlanner planner = createNetwork();

        RoutePlanner.Route route =
                planner.findShortestRoute("Depot", "Main Road");

        check(route.locations().equals(
                        List.of("Depot", "Market", "Main Road")),
                "Choose shorter indirect route over direct road");

        check(Double.compare(route.distanceKm(), 6.0) == 0,
                "Calculate indirect route distance");
    }

    private static void testSameStartAndDestination() {
        RoutePlanner planner = createNetwork();

        RoutePlanner.Route route =
                planner.findShortestRoute("Depot", "Depot");

        check(route.locations().equals(List.of("Depot")),
                "Route to same location");

        check(Double.compare(route.distanceKm(), 0.0) == 0,
                "Same-location distance is zero");
    }

    private static void testUnreachableDestination() {
        RoutePlanner planner = createNetwork();

        expectThrows(IllegalArgumentException.class,
                () -> planner.findShortestRoute("Depot", "Remote Area"),
                "Reject unreachable destination");
    }

    private static void testUnknownLocation() {
        RoutePlanner planner = createNetwork();

        expectThrows(IllegalArgumentException.class,
                () -> planner.findShortestRoute("Unknown", "Depot"),
                "Reject unknown location");
    }

    private static void testInvalidDistance() {
        RoutePlanner planner = createNetwork();

        expectThrows(IllegalArgumentException.class,
                () -> planner.addRoad("Depot", "Hospital", -5.0),
                "Reject negative distance");

        expectThrows(IllegalArgumentException.class,
                () -> planner.addRoad("Depot", "Hospital", 0.0),
                "Reject zero distance");
    }

    private static void testMissingRoadEndpoint() {
        RoutePlanner planner = createNetwork();

        expectThrows(IllegalArgumentException.class,
                () -> planner.addRoad("Depot", "Unknown", 5.0),
                "Reject unregistered road endpoint");
    }

    private static void testLocationCount() {
        RoutePlanner planner = createNetwork();

        check(planner.getLocationCount() == 6,
                "Count registered locations");
    }

    private static void testRouteIsImmutable() {
        RoutePlanner planner = createNetwork();

        RoutePlanner.Route route =
                planner.findShortestRoute("Depot", "Hospital");

        expectThrows(UnsupportedOperationException.class,
                () -> route.locations().add("Extra Location"),
                "Prevent modification of returned route");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }

        passed++;
        System.out.println("PASSED: " + name);
    }

    private static void expectThrows(
            Class<? extends RuntimeException> expected,
            Runnable action,
            String name) {

        try {
            action.run();
        } catch (RuntimeException exception) {
            if (expected.isInstance(exception)) {
                check(true, name);
                return;
            }

            throw new AssertionError(
                    "Wrong exception for: " + name, exception);
        }

        throw new AssertionError(
                "No exception thrown for: " + name);
    }
}
