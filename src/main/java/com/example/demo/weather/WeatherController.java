package com.example.demo.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WeatherController
 *
 * REST endpoints that allow a caller to select a station and a date and get the
 * specific HKO weather information for that combination.
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather/{station}?date=YYYY-MM-DD&dataType=CLMTEMP
     *
     * Retrieve historical daily weather reading(s) for a station on a given date.
     *
     * Examples:
     *   /api/weather/HKO?date=2024-09-05
     *   /api/weather/HKO?date=2024-09-05&dataType=CLMMAXT
     *   /api/weather/SHA?date=2024-09-05&dataType=CLMMINT
     */
    @GetMapping("/{station}")
    public ResponseEntity<Map<String, Object>> getWeather(
            @PathVariable String station,
            @RequestParam String date,
            @RequestParam(required = false, defaultValue = "CLMTEMP") String dataType) {
        try {
            Map<String, Object> result = weatherService.getWeather(station, date, dataType);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(502)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/weather/current/{station}?lang=en
     *
     * Retrieve the real-time temperature / humidity reading for a station.
     *
     * Example: /api/weather/current/HKO
     */
    @GetMapping("/current/{station}")
    public ResponseEntity<Map<String, Object>> getCurrentWeather(
            @PathVariable String station,
            @RequestParam(required = false, defaultValue = "en") String lang) {
        try {
            Map<String, Object> result = weatherService.getCurrentWeather(station, lang);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(502)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/weather/stations
     *
     * List the supported automatic weather station codes and their names.
     */
    @GetMapping("/stations")
    public ResponseEntity<Map<String, String>> listStations() {
        return ResponseEntity.ok(WeatherService.STATIONS);
    }
}