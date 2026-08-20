# Awaitility Test Module

Module:

```text
test:awaitility
```

Use `test:awaitility` when a test must wait for asynchronous behavior.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:awaitility"))
}
```

## Wait until an operation succeeds

```java
ResponseEntity<String> response = Eventually.untilSucceeds(
        Duration.ofSeconds(30),
        Duration.ofMillis(250),
        () -> client.call()
);
```

## Wait until an assertion passes

```java
Eventually.untilAsserted(
        Duration.ofSeconds(10),
        Duration.ofMillis(100),
        () -> assertEquals(1, repository.count())
);
```

## Wait until a condition is true

```java
Eventually.until(
        Duration.ofSeconds(10),
        Duration.ofMillis(100),
        () -> listener.hasReceived("policy-1")
);
```

## Wait until an expected exception appears

```java
IllegalStateException exception = Eventually.untilThrows(
        Duration.ofSeconds(10),
        Duration.ofMillis(100),
        IllegalStateException.class,
        () -> client.callUnavailableService(),
        thrown -> thrown.getMessage().contains("unavailable")
);
```

## Rule

Use `Eventually` instead of `Thread.sleep(...)`. A test should wait for a real
condition, not for a guessed amount of time.
