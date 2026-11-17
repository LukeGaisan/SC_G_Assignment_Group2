package com.stoneflamepath.api;

import com.stoneflamepath.api.NominatimClient.HttpReply;
import com.stoneflamepath.model.Coordinate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NominatimClientTest {

    private static final double DELTA = 0.0001;
    private static final String PJ_JSON = "[{\"lat\":\"3.1073\",\"lon\":\"101.6067\"}]";

    private static NominatimClient clientReturning(int status, String body) {
        return new NominatimClient(uri -> new HttpReply(status, body), 0);
    }

    private static NominatimClient clientWithNetworkDown() {
        return new NominatimClient(uri -> {
            throw new IOException("network down");
        }, 0);
    }

    @Test
    void geocode_validResponse_returnsParsedCoordinate() throws GeocodingException {
        Optional<Coordinate> result = clientReturning(200, PJ_JSON).geocode("Petaling Jaya");

        assertTrue(result.isPresent());
        assertAll(
                () -> assertEquals(3.1073, result.get().getLatitude(), DELTA),
                () -> assertEquals(101.6067, result.get().getLongitude(), DELTA));
    }

    @Test
    void geocode_emptyResultList_returnsEmpty() throws GeocodingException {
        assertTrue(clientReturning(200, "[]").geocode("nowhere land").isEmpty());
    }

    @Test
    void geocode_status404_returnsEmpty() throws GeocodingException {
        assertTrue(clientReturning(404, "").geocode("nowhere land").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503})
    void geocode_serverErrorStatus_throwsFriendlyException(int status) {
        GeocodingException error = assertThrows(GeocodingException.class,
                () -> clientReturning(status, "").geocode("Petaling Jaya"));

        assertFalse(error.getMessage().contains("Exception"));
    }

    @Test
    void geocode_networkDownWithNoSavedResult_throwsFriendlyException() {
        GeocodingException error = assertThrows(GeocodingException.class,
                () -> clientWithNetworkDown().geocode("Petaling Jaya"));

        assertTrue(error.getMessage().contains("not available"));
    }

    @Test
    void geocode_networkDownAfterEarlierSuccess_returnsLastKnownLocation() throws GeocodingException {
        AtomicInteger calls = new AtomicInteger();
        NominatimClient client = new NominatimClient(uri -> {
            if (calls.getAndIncrement() == 0) {
                return new HttpReply(200, PJ_JSON);
            }
            throw new IOException("network down");
        }, 0);
        client.geocode("Petaling Jaya");

        Optional<Coordinate> second = client.geocode("Petaling Jaya");

        assertTrue(second.isPresent());
        assertTrue(client.isLastResultCached());
    }

    @Test
    void geocode_malformedJson_throwsGeocodingException() {
        assertThrows(GeocodingException.class,
                () -> clientReturning(200, "<html>oops</html>").geocode("Petaling Jaya"));
    }

    @Test
    void geocode_blankAddress_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> clientReturning(200, PJ_JSON).geocode("   "));
    }

    @Test
    void geocode_anyAddress_sendsEncodedQueryWithJsonFormat() throws GeocodingException {
        AtomicReference<URI> sent = new AtomicReference<>();
        NominatimClient client = new NominatimClient(uri -> {
            sent.set(uri);
            return new HttpReply(200, PJ_JSON);
        }, 0);

        client.geocode("SS2 Bus Hub");

        assertAll(
                () -> assertTrue(sent.get().toString().contains("q=ss2+bus+hub")),
                () -> assertTrue(sent.get().toString().contains("format=json")));
    }
}
