# springboot-api-sample

A minimal **Spring Boot 3.2 + Java 21** REST API sample, built with Maven.

## Requirements
- JDK 21
- Maven 3.8+

## Build
```bash
mvn clean package -DskipTests
```

## Run
```bash
java -jar target/springboot-api-sample-1.0.0.jar
```
The service starts on **http://localhost:8080**.

## Endpoints
| Method | Path                    | Description               |
|--------|-------------------------|---------------------------|
| GET    | `/api/hello?name=X`     | Greeting with an ID + timestamp |
| GET    | `/api/info`             | App info / health status  |
| POST   | `/api/echo`             | Echoes back the request body |
| GET    | `/api/add?a=1&b=2`      | Returns the sum of `a + b` |
| GET    | `/api/greet/{name}`     | Greeting with a path variable |
| GET    | `/actuator/health`      | Spring Actuator health check |

## License
MIT