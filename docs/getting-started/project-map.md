# Project Map

Use this page to find the module that provides the capability you need.

## Production modules

| Module | Owns | Use when |
| --- | --- | --- |
| `lib:common` | Small reusable utilities. | A helper is useful across unrelated modules. |
| `lib:cache:api` | Cache contract. | Code needs cache behavior without knowing Redis. |
| `lib:cache:core` | In-memory and Redis cache implementations. | A Spring app needs concrete cache storage. |
| `lib:idempotency:core` | HTTP idempotency for Spring MVC. | A command endpoint must be safe to retry. |
| `lib:data` | Generic JPA/QueryDSL/Entity View support. | A JPA service follows common repository patterns. |
| `lib:search:api` | Search-facing contracts. | Code needs search abstractions without depending on Elasticsearch infrastructure. |
| `lib:search:core` | Elasticsearch search services and query helpers. | A module needs full-text or indexed document search. |
| `lib:event:api` | Domain event contracts. | A module publishes or consumes typed domain events. |
| `lib:event:core` | Spring/NATS/Kafka event infrastructure. | Events must be routed through Spring, NATS, or Kafka. |
| `lib:stream:api` | Client stream abstractions. | A feature exposes resource or collection updates. |
| `lib:stream:core` | SSE stream implementation. | A Spring service exposes server-sent events. |
| `lib:security:api` | Shared security context and Keycloak client contract. | Code needs roles, claims, or auth client API. |
| `lib:security:core` | Keycloak and Spring Security integration. | A service authenticates through Keycloak. |
| `lib:eureka:api` | Eureka-facing contracts. | Code needs discovery-related API without depending on the concrete integration. |
| `lib:eureka:core` | Eureka integration support. | A service participates in discovery. |
| `lib:fault-tolerance:core` | Circuit breaker defaults. | A service calls another service and needs failure isolation. |

Production modules follow a consistent ownership rule: `api` modules define
stable contracts for consumers, while `core` modules provide Spring and
infrastructure implementations. Single-module libraries such as `lib:common`,
`lib:data`, `lib:idempotency:core`, and `lib:fault-tolerance:core` own both the
public contract and implementation for their focused capability.

## Test modules

| Module | Use when |
| --- | --- |
| `test:common` | Building reusable Testcontainers modules. |
| `test:awaitility` | Waiting for asynchronous behavior in tests. |
| `test:redis` | Testing real Redis behavior. |
| `test:kafka` | Testing real Kafka publishing or listening. |
| `test:nats` | Testing real NATS broadcast publishing or listening. |
| `test:postgresql` | Testing real database mappings or queries. |
| `test:elasticsearch` | Testing real Elasticsearch indexing or queries. |
| `test:keycloak` | Testing real Keycloak integration. |
| `test:eureka` | Testing real service registration or discovery. |

Test modules provide reusable infrastructure for behavior tests. Prefer these
modules over duplicated container setup in production library tests.
