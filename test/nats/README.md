# NATS test module

`test:nats` provides a reusable NATS Testcontainers setup for integration tests.
Activate the `test-nats` profile and set
`ravcube.testcontainers.nats.enabled=true` to start a shared NATS server and
publish its URL as `ravcube.nats.url`.
