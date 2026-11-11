package com.roomsetu.util;

public final class Geo {

    private Geo() {}

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** A rough band instead of an exact distance, so nobody can pin down another person's location. */
    public static String distanceLabel(double km) {
        if (km < 2) return "Under 2 km";
        if (km < 5) return "2-5 km";
        if (km < 10) return "5-10 km";
        if (km < 25) return "10-25 km";
        return "25+ km";
    }
}