# Fault Tolerance Core

Spring Cloud CircuitBreaker + Resilience4j defaults for HTTP clients.

Use this module with the `fault-tolerance` Spring profile. For OpenFeign clients, the profile enables Feign circuit breaker support:

```yaml
spring:
  profiles:
    active: eureka,fault-tolerance
```

The module is independent from Eureka. With Eureka and Spring Cloud LoadBalancer it protects discovered Feign clients; with a direct Feign URL it protects the same client call path.

This module does not replay old requests in the background. When a downstream is unavailable, the current Feign call should fail fast or return a fallback response. A later, new Feign call can pass once the downstream is available again and the circuit breaker closes.
