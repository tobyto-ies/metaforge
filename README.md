# springboot-api-sample

A minimal **Spring Boot 3.2 + Java 17** REST API sample, built with Maven.

## Requirements
- JDK 17
- Maven 3.8+

## Build
```bash
mvn clean package -DskipTests
```

## Run
```bash
java -jar target/springboot-api-sample-1.0.0.jar
```
The service starts on **http://localhost:8090**.

## Endpoints
| Method | Path                    | Description               |
|--------|-------------------------|---------------------------|
| GET    | `/api/hello?name=X`     | Greeting with an ID + timestamp |
| GET    | `/api/info`             | App info / health status  |
| POST   | `/api/echo`             | Echoes back the request body |
| GET    | `/api/add?a=1&b=2`      | Returns the sum of `a + b` |
| GET    | `/api/greet/{name}`     | Greeting with a path variable |
| GET    | `/api/time`             | Current time (UTC, epoch, local) |
| GET    | `/actuator/health`      | Spring Actuator health check |

## Weather API (HKO Open Data)

Select an automatic weather station and a date to retrieve the specific weather
information published by the Hong Kong Observatory (HKO) Open Data API
(see [HKO_Open_Data_API_Documentation](https://www.hko.gov.hk/en/weatherAPI/doc/files/HKO_Open_Data_API_Documentation.pdf)).

| Method | Path                                                  | Description                                   |
|--------|-------------------------------------------------------|-----------------------------------------------|
| GET    | `/api/weather/{station}?date=YYYY-MM-DD`              | Daily Mean Temperature for station + date     |
| GET    | `/api/weather/{station}?date=YYYY-MM-DD&dataType=X`   | Specific metric (dataType) for station + date |
| GET    | `/api/weather/current/{station}?lang=en`              | Real-time temperature / humidity for station  |
| GET    | `/api/weather/stations`                               | List supported station codes and names        |

### `dataType` values
| value     | description                        |
|-----------|------------------------------------|
| `CLMTEMP` | Daily Mean Temperature             |
| `CLMMAXT` | Daily Maximum Temperature          |
| `CLMMINT` | Daily Minimum Temperature          |
| `HHOT`    | Hourly heights of astronomical tides |
| `SRS`     | Times of sunrise / sunset          |
| `MRS`     | Times of moonrise / moonset        |
| `LTMV`    | Latest 10-minute mean visibility   |
| `RYES`    | Weather and Radiation Level Report |

### Examples
```bash
# Daily mean temperature at Hong Kong Observatory on 2024-09-05
curl "http://localhost:8090/api/weather/HKO?date=2024-09-05"

# Daily maximum temperature at Sha Tin on 2024-09-05
curl "http://localhost:8090/api/weather/SHA?date=2024-09-05&dataType=CLMMAXT"

# Real-time temperature / humidity at the Observatory
curl "http://localhost:8090/api/weather/current/HKO"

# List all supported stations
curl "http://localhost:8090/api/weather/stations"
```

### Supported station codes (subset)
`HKO` (Hong Kong Observatory), `SHA` (Sha Tin), `TKL` (Ta Kwu Ling),
`KLT` (Kowloon City), `KP` (King's Park), `TW` (Tsuen Wan), `TMS` (Tai Mo Shan),
`SSH` (Sheung Shui), `WGL` (Waglan Island), `VP1` (The Peak),
`TYW` (Pak Tam Chung), `LFS` (Lau Fau Shan)

## License
MIT
