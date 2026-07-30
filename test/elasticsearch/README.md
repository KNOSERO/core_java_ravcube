# test:elasticsearch

Wzor tworzenia modulow `test:*` jest opisany w [test/common/README.md](../common/README.md).

Ten modul dostarcza gotowy kontener Elasticsearch do uzycia w testach innych modulow.

## Jak dodac do testow

Dodaj zaleznosc:

```kotlin
testImplementation(project(":test:elasticsearch"))
```

Aktywuj profile:

```java
import static com.ravcube.test.elasticsearch.ElasticsearchTestProfiles.TEST_ELASTICSEARCH_PROFILE;

@ActiveProfiles({"elasticsearch", TEST_ELASTICSEARCH_PROFILE})
```

## Co dostajesz z modulu

- start wspolnego kontenera Elasticsearch
- automatyczne ustawienie:
  - `ravcube.search.uris`

## Czego nie kopiowac do testu

Nie ustawiaj recznie:

- `ravcube.search.uris`

To ma byc utrzymywane w tym module.
