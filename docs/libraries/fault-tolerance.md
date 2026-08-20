# Fault Tolerance

Module:

```text
lib:fault-tolerance:core
```

This module provides Spring Cloud CircuitBreaker and Resilience4j defaults for
downstream service calls.

## Use When

Use it when a service calls another service and the caller needs predictable
failure behavior.

## Enable

```yaml
spring:
  profiles:
    active: fault-tolerance
```

With service discovery:

```yaml
spring:
  profiles:
    active: eureka,fault-tolerance
```

## Behavior

The module protects the current call path. It does not replay old requests in
the background. When a downstream service is unavailable, the current call fails
fast or uses a configured fallback. A later call can pass after the circuit
breaker closes.

## Configuration

The profile enables OpenFeign circuit breaker integration:

| Property | Default |
| --- | --- |
| `ravcube.fault-tolerance.openfeign.circuitbreaker.enabled` | `true` |

Circuit breaker defaults:

| Property | Default |
| --- | --- |
| `ravcube.fault-tolerance.circuitbreaker.sliding-window-type` | `COUNT_BASED` |
| `ravcube.fault-tolerance.circuitbreaker.sliding-window-size` | `10` |
| `ravcube.fault-tolerance.circuitbreaker.minimum-number-of-calls` | `5` |
| `ravcube.fault-tolerance.circuitbreaker.failure-rate-threshold` | `50` |
| `ravcube.fault-tolerance.circuitbreaker.wait-duration-in-open-state` | `10s` |
| `ravcube.fault-tolerance.circuitbreaker.permitted-number-of-calls-in-half-open-state` | `3` |
| `ravcube.fault-tolerance.circuitbreaker.automatic-transition-from-open-to-half-open-enabled` | `true` |

Use with Eureka when Feign clients should resolve service names. Use without
Eureka when Feign clients call direct URLs.
