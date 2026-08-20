# Common

Module:

```text
lib:common
```

`lib:common` contains small utilities that are useful across unrelated modules.
Keep this module narrow. A class belongs here only when it has no dependency on
Spring, persistence, messaging, HTTP, or a specific business concept.

## UniqueIndex

`UniqueIndex` builds an immutable map and rejects duplicate keys.

Use it when duplicate keys are a programming or configuration error.

```java
Map<String, Handler> handlers = UniqueIndex.by(
        handlerList,
        Handler::resourceName,
        duplicate -> "Duplicate handler for resource: " + duplicate
);
```

## Contract

- input collection cannot be `null`,
- collection entries cannot be `null`,
- extracted keys cannot be `null`,
- duplicate keys throw `IllegalArgumentException`,
- returned map is immutable.

## Typical Use

Use it for handler registries, named strategy collections, and configuration
maps where duplicate entries should fail during startup instead of producing
last-one-wins behavior.
