# Elasticsearch Test Module

Module:

```text
test:elasticsearch
```

Use this module when a test needs a real Elasticsearch instance.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:elasticsearch"))
}
```

## Profile

```java
import static com.ravcube.test.elasticsearch.ElasticsearchTestProfiles.TEST_ELASTICSEARCH_PROFILE;

@ActiveProfiles({"elasticsearch", TEST_ELASTICSEARCH_PROFILE})
```

## What this module configures

Injected properties:

| Property | Value |
| --- | --- |
| `ravcube.search.uris` | HTTP URI for the Elasticsearch Testcontainer. |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.elasticsearch.enabled` | `true` |
| `ravcube.testcontainers.elasticsearch.image` | `docker.elastic.co/elasticsearch/elasticsearch:8.14.3` |

The container starts as a single-node Elasticsearch instance with xpack security
disabled. Use the production `elasticsearch` profile together with
`TEST_ELASTICSEARCH_PROFILE`.

## Good use cases

- index mapping,
- search query execution,
- nested queries,
- range and full-text behavior.

Do not use the container to test simple `SearchPredicate` composition if that
can be verified without Elasticsearch.
