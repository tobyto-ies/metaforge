package com.example.demo.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WeatherService
 *
 * Wraps the Hong Kong Observatory (HKO) Open Data API so that a caller can
 * select an automatic weather station and a date, and retrieve the specific
 * weather information published for that station/date.
 *
 * Primary endpoint used: Open Data (Climate and Weather Information) API
 *   https://data.weather.gov.hk/weatherAPI/opendata/opendata.php
 *
 * Supported data types (dataType):
 *   - CLMTEMP : Daily Mean Temperature
 *   - CLMMAXT : Daily Maximum Temperature
 *   - CLMMINT : Daily Minimum Temperature
 *   - HHOT    : Hourly heights of astronomical tides (station + date)
 *   - SRS     : Times of sunrise/sunset
 *   - MRS     : Times of moonrise/moonset
 *   - LTMV    : Latest 10-minute mean visibility
 *   - RYES    : Weather and Radiation Level Report
 *
 * Current Weather Report endpoint (real-time, station inside response):
 *   https://data.weather.gov.hk/weatherAPI/opendata/weather.php?dataType=rhrread
 */
@Service
public class WeatherService {

    /** Base URL of the HKO Open Data (climate) API. */
    private static final String OPENDATA_BASE_URL =
            "https://data.weather.gov.hk/weatherAPI/opendata/opendata.php";

    /** Base URL of the HKO current weather API. */
    private static final String WEATHER_BASE_URL =
            "https://data.weather.gov.hk/weatherAPI/opendata/weather.php";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** List of valid station codes we accept (subset of the HKO automatic weather stations). */
    public static final Map<String, String> STATIONS = buildStations();

    /** Valid dataTypes exposed by this service. */
    public static final List<String> DATA_TYPES = List.of(
            "CLMTEMP", "CLMMAXT", "CLMMINT", "HHOT", "SRS", "MRS", "LTMV", "RYES");

    private static Map<String, String> buildStations() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("HKO", "Hong Kong Observatory");
        map.put("SHA", "Sha Tin");
        map.put("TKL", "Ta Kwu Ling");
        map.put("KLT", "Kowloon City");
        map.put("KP", "King's Park");
        map.put("TW", "Tsuen Wan");
        map.put("TMS", "Tai Mo Shan");
        map.put("SSH", "Sheung Shui");
        map.put("WGL", "Waglan Island");
        map.put("VP1", "The Peak");
        map.put("TYW", "Pak Tam Chung");
        map.put("LFS", "Lau Fau Shan");
        return map;
    }

    /**
     * Retrieve weather information for a given station and full date (YYYY-MM-DD).
     *
     * @param station  automatic weather station code, e.g. "HKO", "SHA"
     * @param date     date in ISO format "YYYY-MM-DD"
     * @param dataType one of the DATA_TYPES, e.g. "CLMTEMP"; if blank defaults to CLMTEMP
     * @return a JSON-friendly map containing the station, the query params sent to
     *         HKO, the resolved records filtered to the requested date, and the
     *         raw upstream payload
     */
    public Map<String, Object> getWeather(String station, String date, String dataType) {
        LocalDate d = parseDate(date);
        String dt = (dataType == null || dataType.isBlank()) ? "CLMTEMP" : dataType.toUpperCase();

        if (!STATIONS.containsKey(station)) {
            throw new IllegalArgumentException(
                    "Unknown station '" + station + "'. Valid stations: " + STATIONS.keySet());
        }
        if (!DATA_TYPES.contains(dt)) {
            throw new IllegalArgumentException(
                    "Unknown dataType '" + dt + "'. Valid dataTypes: " + DATA_TYPES);
        }

        // Build the upstream HKO request URL.
        String url = UriComponentsBuilder.fromHttpUrl(OPENDATA_BASE_URL)
                .queryParam("dataType", dt)
                .queryParam("station", station)
                .queryParam("year", d.getYear())
                .queryParam("month", d.getMonthValue())
                .queryParam("rformat", "json")
                .build()
                .toUriString();

        JsonNode root;
        try {
            String body = restTemplate.getForObject(url, String.class);
            root = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call HKO API at " + url + ": " + e.getMessage(), e);
        }

        // Parse the JSON columnar format {type, fields, data, legend}.
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("station", station);
        result.put("stationName", STATIONS.get(station));
        result.put("dataType", dt);
        result.put("date", d.toString());

        List<String> fields = new ArrayList<>();
        if (root.has("fields")) {
            root.get("fields").forEach(f -> fields.add(f.asText()));
        }
        result.put("fields", fields);

        // Filter rows to the requested day (fields[0]=year, fields[1]=month, fields[2]=day).
        List<List<String>> matchedRows = new ArrayList<>();
        if (root.has("data")) {
            for (JsonNode row : root.get("data")) {
                if (!row.isArray() || row.size() < 3) continue;
                String y = row.get(0).asText();
                String m = row.get(1).asText();
                String day = row.get(2).asText();
                if (Integer.parseInt(y) == d.getYear()
                        && Integer.parseInt(m) == d.getMonthValue()
                        && Integer.parseInt(day) == d.getDayOfMonth()) {
                    List<String> rowList = new ArrayList<>();
                    row.forEach(c -> rowList.add(c.asText()));
                    matchedRows.add(rowList);
                }
            }
        }
        result.put("records", matchedRows);
        result.put("recordCount", matchedRows.size());

        List<String> legend = new ArrayList<>();
        if (root.has("legend")) {
            root.get("legend").forEach(l -> legend.add(l.asText()));
        }
        result.put("legend", legend);
        result.put("raw", root);
        return result;
    }

    /**
     * Retrieve the real-time current weather report (temperature / humidity by
     * station). Shows the reading for the requested station when present.
     */
    public Map<String, Object> getCurrentWeather(String station, String lang) {
        String l = (lang == null || lang.isBlank()) ? "en" : lang.toLowerCase();
        String url = UriComponentsBuilder.fromHttpUrl(WEATHER_BASE_URL)
                .queryParam("dataType", "rhrread")
                .queryParam("lang", l)
                .build()
                .toUriString();

        JsonNode root;
        try {
            String body = restTemplate.getForObject(url, String.class);
            root = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call HKO current weather API: " + e.getMessage(), e);
        }

        String displayName = STATIONS.getOrDefault(station, station);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("station", station);
        result.put("stationName", displayName);
        result.put("temperature", findStationReading(root, "temperature", displayName));
        result.put("humidity", findStationReading(root, "humidity", displayName));
        result.put("updateTime", root.has("updateTime") ? root.get("updateTime").asText() : null);
        return result;
    }

    /**
     * Look up the reading record for a given station (matched by its display name,
     * e.g. "Hong Kong Observatory") inside a weather.php section such as
     * "temperature" or "humidity".
     */
    private Map<String, Object> findStationReading(JsonNode root, String section, String displayName) {
        Map<String, Object> reading = new LinkedHashMap<>();
        if (root.has(section) && root.get(section).has("data")) {
            for (JsonNode node : root.get(section).get("data")) {
                if (node.has("place") && displayName != null
                        && displayName.equalsIgnoreCase(node.get("place").asText())) {
                    reading.put("place", node.path("place").asText());
                    reading.put("value", node.path("value").asText(null));
                    reading.put("unit", node.path("unit").asText(null));
                    return reading;
                }
            }
        }
        reading.put("note", "Station '" + displayName + "' not found in current report.");
        return reading;
    }

    /** Parse an ISO date. Throws IllegalArgumentException with a helpful message. */
    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("Date is required (format YYYY-MM-DD).");
        }
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date '" + date + "'. Expected format YYYY-MM-DD, e.g. 2024-09-05.", e);
        }
    }
}