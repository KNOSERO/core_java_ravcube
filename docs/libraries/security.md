# Security

Modules:

```text
lib:security:api
lib:security:core
```

The security modules integrate Spring Security with Keycloak and expose a small
request-scoped security context.

## Responsibilities

| Module | Responsibility |
| --- | --- |
| `lib:security:api` | `SecurityContext` and Keycloak client contract. |
| `lib:security:core` | Spring Security configuration and auth endpoints. |

## SecurityContext

`SecurityContext` stores roles and claims for the current request flow.

```java
List<String> roles = SecurityContext.getRoles();
Map<String, Object> claims = SecurityContext.getClaims();
```

Infrastructure must clear the context after request processing.

## Auth Endpoints

Default path:

```text
/auth
```

Endpoints:

```http
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

Request and response DTOs:

| Type | Fields |
| --- | --- |
| `LoginRequest` | `username`, `password` |
| `RefreshTokenRequest` | `refreshToken` |
| `TokenResponse` | `accessToken`, `refreshToken` |

## Configuration

Enable Keycloak integration with the `keycloak` Spring profile.

```yaml
spring:
  profiles:
    active: keycloak
```

Important properties:

| Property | Default | Purpose |
| --- | --- | --- |
| `ravcube.keycloak.issuer-uri` | `http://localhost:8080/realms/master` | JWT issuer and Keycloak realm URL. |
| `ravcube.keycloak.client-id` | `admin-cli` | Client id used for password, refresh, and logout calls. |
| `ravcube.security.auth.path` | `/auth` | Base path for auth endpoints and permit-all matchers. |

`KeycloakAuthClient` calls:

```text
{issuer-uri}/protocol/openid-connect/token
{issuer-uri}/protocol/openid-connect/logout
```

`KeycloakAuthService` maps Keycloak `access_token` and `refresh_token` fields
into `TokenResponse`. Feign errors from Keycloak are propagated as
`ResponseStatusException` with the Keycloak status when possible.

## Security filter

The default `SecurityFilterChain` permits login, refresh, and logout. Other
requests require authentication. `SecurityContextTokenFilter` copies JWT claims
and granted authorities into `SecurityContext` for the request and clears the
thread-local context in `finally`.

## Testing

Use `test:keycloak` when a test needs real token behavior, Keycloak responses,
or Spring Security integration with a real identity provider.
