package com.stoneflamepath.api;

import com.stoneflamepath.model.Coordinate;

import java.util.Optional;

/**
 * Turns a place name typed by the user into a point on the map. The rest of the
 * system depends on this interface, never on a particular map provider.
 */
public interface GeocodingService {

    /**
     * Looks up a place name.
     *
     * @param address the place name or address to find
     * @return the location, or empty if the service has no match for it
     * @throws GeocodingException   if the service is unavailable and nothing was saved earlier
     * @throws IllegalArgumentException if {@code address} is null or blank
     */
    Optional<Coordinate> geocode(String address) throws GeocodingException;

    /**
     * Tells the caller whether the last answer came from saved data because
     * the service could not be reached.
     *
     * @return true if the last result was the last known location, not a fresh one
     */
    default boolean isLastResultCached() {
        return false;
    }
}
