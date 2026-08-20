# Eureka

Modules:

```text
lib:eureka:api
lib:eureka:core
```

Eureka support is used when services discover other services by name instead of
calling fixed URLs.

## Use When

Use Eureka when an application needs service discovery through Spring Cloud
Netflix Eureka.

It is commonly combined with:

- OpenFeign clients,
- Spring Cloud LoadBalancer,
- `lib:fault-tolerance:core`,
- `test:eureka` for integration tests.

## Configuration

Enable Eureka support with the `eureka` Spring profile.

```yaml
spring:
  profiles:
    active: eureka
```

Important properties:

| Property | Default |
| --- | --- |
| `ravcube.eureka.app-name` | `ravcube-service` |
| `ravcube.eureka.default-zones` | `http://localhost:8761/eureka/` |
| `ravcube.eureka.client.fetch-registry` | `true` |
| `ravcube.eureka.client.register-with-eureka` | `true` |
| `ravcube.eureka.client.registry-fetch-interval-seconds` | `5` |
| `ravcube.eureka.instance.lease-renewal-interval-in-seconds` | `10` |
| `ravcube.eureka.instance.lease-expiration-duration-in-seconds` | `30` |

The module maps these values into Spring Cloud Netflix Eureka client settings.

## Testing

Use `test:eureka` when the behavior depends on real registration or discovery.
Do not use Eureka tests for plain HTTP client logic.
