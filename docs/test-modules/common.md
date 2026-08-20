# Common Test Infrastructure

Module:

```text
test:common
```

This module contains shared infrastructure for Testcontainers modules.

## Main types

| Type | Purpose |
| --- | --- |
| `SharedContainer` | Starts and reuses a single container instance. |
| `SharedContainerCluster` | Starts and reuses multiple related containers. |
| `BaseEnvironmentPostProcessor` | Base Spring environment helper. |
| `BaseTestcontainerEnvironmentPostProcessor` | Starts a container and adds Spring properties when a test profile is active. |
| `TestDelays` | Small delay helper for tests that intentionally need delayed actions. |

## When adding a new test module

Use `BaseTestcontainerEnvironmentPostProcessor` when the module has one simple
container. Use `BaseEnvironmentPostProcessor` directly only when the module has
custom startup logic.

## What belongs here

- shared container startup,
- profile activation checks,
- property-source registration,
- reusable waiting behavior.

What does not belong here:

- domain-specific test assertions,
- one module's local test data,
- copied application configuration.

## Shared container behavior

`SharedContainer` and `SharedContainerCluster` reuse containers inside one JVM.
They pin the requested image name. If a later test requests a different image
for the same shared container, startup fails instead of silently mixing test
infrastructure.

`BaseTestcontainerEnvironmentPostProcessor` starts only when:

- the module test profile is active;
- the module enabled property is `true` or missing.

It resolves the image from a module-specific property, starts the container, and
adds a first-priority Spring property source with connection properties.
