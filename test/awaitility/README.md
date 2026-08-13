# test:awaitility

Shared waiting helpers based on Awaitility.

## Usage

Add the module to tests:

```kotlin
testImplementation(project(":test:awaitility"))
```

Use `Eventually` instead of calling Awaitility directly from every test:

```java
ResponseEntity<String> response = Eventually.untilSucceeds(
        Duration.ofSeconds(30),
        Duration.ofMillis(250),
        () -> client.call()
);
```
