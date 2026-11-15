package com.stoneflamepath.model;

/**
 * An immutable point on the map, given as latitude and longitude in degrees.
 */
public final class Coordinate {

    private static final double EARTH_DIAMETER_KM = 12742.0;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double HALF = 0.5;

    private final double latitude;
    private final double longitude;

    /**
     * Creates a coordinate.
     *
     * @param latitude  latitude in degrees, between -90 and 90
     * @param longitude longitude in degrees, between -180 and 180
     * @throws IllegalArgumentException if either value is out of range
     */
    public Coordinate(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** @return latitude in degrees */
    public double getLatitude() {
        return latitude;
    }

    /** @return longitude in degrees */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Calculates the straight-line (great-circle) distance to another coordinate
     * using the haversine formula.
     *
     * @param other the other coordinate
     * @return distance in kilometres
     */
    public double distanceTo(Coordinate other) {
        double deltaLat = Math.toRadians(other.latitude - latitude);
        double deltaLon = Math.toRadians(other.longitude - longitude);
        double sinLat = Math.sin(deltaLat * HALF);
        double sinLon = Math.sin(deltaLon * HALF);
        double haversine = sinLat * sinLat
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * sinLon * sinLon;
        return EARTH_DIAMETER_KM * Math.asin(Math.sqrt(haversine));
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
