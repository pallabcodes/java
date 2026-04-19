package com.backend.math;

/**
 * Step 05: Geo-Spatial Engineering (Haversine Distance)
 * 
 * L5 Principles:
 * 1. Spherical Geometry: Earth is not flat; distance math must account for curvature.
 * 2. Precision: Using radians and double-precision for accurate GPS calculations.
 * 3. Domain Context: Critical for Maps, Uber-style matching, and Local search.
 */
public class Step05_HaversineDistance {

    private static final double EARTH_RADIUS = 6371; // Kilometres

    /**
     * Calculates the great-circle distance between two points on a sphere.
     */
    public static double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLon = Math.toRadians(endLon - startLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static void main(String[] args) {
        System.out.println("=== Step 05: Haversine Distance (Maps Math) ===");

        // Googleplex (Mountain View)
        double plexLat = 37.4220;
        double plexLon = -122.0841;

        // San Francisco (Ferry Building)
        double sfLat = 37.7955;
        double sfLon = -122.3937;

        double distance = calculateDistance(plexLat, plexLon, sfLat, sfLon);

        System.out.printf("Distance from Googleplex to SF: %.2f km\n", distance);
        System.out.println("L5 Note: Haversine is the industry standard for fast, spherical distance math.");
    }
}
