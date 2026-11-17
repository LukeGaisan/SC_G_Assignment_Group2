package com.stoneflamepath.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.stoneflamepath.model.Coordinate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Finds places using the free OpenStreetMap Nominatim search service.
 * It follows Nominatim's usage policy (custom User-Agent, at most one request
 * per second) and keeps the last known result for each place so the app still
 * works when the service is down.
 */
public class NominatimClient implements GeocodingService {

    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String QUERY_OPTIONS = "&format=json&limit=1&countrycodes=my";
    private static final String USER_AGENT = "StoneFlamePath/1.0 (university software construction project)";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final long MIN_INTERVAL_MILLIS = 1000;
    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;
    private static final String UNAVAILABLE_MESSAGE =
            "The map service is not available right now. Please try again in a moment.";
    private static final String UNREADABLE_MESSAGE =
            "The map service sent an answer we could not understand. Please try again.";

    /** A minimal HTTP answer: status code and body text. */
    public record HttpReply(int status, String body) { }

    /** Sends one GET request. Replaced by a stub in unit tests. */
    @FunctionalInterface
    public interface HttpSender {
        /**
         * Sends a GET request.
         *
         * @param uri the address to request
         * @return the status code and body
         * @throws IOException          if the network fails or times out
         * @throws InterruptedException if the thread is interrupted while waiting
         */
        HttpReply get(URI uri) throws IOException, InterruptedException;
    }

    private final HttpSender sender;
    private final long minIntervalMillis;
    private final Map<String, Coordinate> lastKnown = new HashMap<>();
    private long lastRequestAt;
    private boolean lastResultCached;

    /** Creates a client that talks to the real Nominatim service. */
    public NominatimClient() {
        this(javaHttpSender(HttpClient.newBuilder().connectTimeout(TIMEOUT).build()),
                MIN_INTERVAL_MILLIS);
    }

    NominatimClient(HttpSender sender, long minIntervalMillis) {
        this.sender = sender;
        this.minIntervalMillis = minIntervalMillis;
    }

    @Override
    public synchronized Optional<Coordinate> geocode(String address) throws GeocodingException {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address must not be blank");
        }
        String key = address.trim().toLowerCase();
        lastResultCached = false;
        try {
            Optional<Coordinate> result = interpret(fetch(key));
            result.ifPresent(found -> lastKnown.put(key, found));
            return result;
        } catch (IOException | GeocodingException failure) {
            return fallbackToLastKnown(key, failure);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return fallbackToLastKnown(key, interrupted);
        }
    }

    @Override
    public synchronized boolean isLastResultCached() {
        return lastResultCached;
    }

    private HttpReply fetch(String key) throws IOException, InterruptedException {
        waitForRateLimit();
        String encoded = URLEncoder.encode(key, StandardCharsets.UTF_8);
        return sender.get(URI.create(SEARCH_URL + "?q=" + encoded + QUERY_OPTIONS));
    }

    private void waitForRateLimit() throws InterruptedException {
        long remaining = minIntervalMillis - (System.currentTimeMillis() - lastRequestAt);
        if (remaining > 0) {
            Thread.sleep(remaining);
        }
        lastRequestAt = System.currentTimeMillis();
    }

    private Optional<Coordinate> interpret(HttpReply reply) throws GeocodingException {
        if (reply.status() == HTTP_OK) {
            return parseFirstResult(reply.body());
        }
        if (reply.status() == HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        throw new GeocodingException(UNAVAILABLE_MESSAGE);
    }

    private Optional<Coordinate> parseFirstResult(String body) throws GeocodingException {
        try {
            JsonArray results = JsonParser.parseString(body).getAsJsonArray();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            JsonObject first = results.get(0).getAsJsonObject();
            return Optional.of(new Coordinate(
                    first.get("lat").getAsDouble(), first.get("lon").getAsDouble()));
        } catch (JsonParseException | IllegalStateException | NullPointerException
                 | IllegalArgumentException badData) {
            throw new GeocodingException(UNREADABLE_MESSAGE, badData);
        }
    }

    private Optional<Coordinate> fallbackToLastKnown(String key, Exception cause)
            throws GeocodingException {
        Coordinate saved = lastKnown.get(key);
        if (saved == null) {
            throw new GeocodingException(UNAVAILABLE_MESSAGE, cause);
        }
        lastResultCached = true;
        return Optional.of(saved);
    }

    private static HttpSender javaHttpSender(HttpClient client) {
        return uri -> {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new HttpReply(response.statusCode(), response.body());
        };
    }
}
