# AI Development Guidelines

This repository contains Java and Kotlin libraries focused on Spring integration,
infrastructure support, and reusable domain-oriented building blocks. AI agents
working in this repository must optimize for maintainable library code, clear
domain boundaries, and documentation that can be reused by both humans and AI.

## Engineering Principles

- Apply KISS: prefer simple, explicit implementations over clever abstractions.
- Apply DRY with judgment: remove meaningful duplication, but do not introduce
  abstractions before a stable pattern exists.
- Apply SOLID where it improves maintainability, testability, and boundaries.
- Before adding a new implementation, inspect the existing modules for reusable
  abstractions, adapters, utilities, and extension points.
- Prefer extending or lightly adapting existing implementations over creating a
  parallel solution from scratch.
- When an existing implementation is close but incomplete, evolve it so it can
  serve the new use case and remain reusable for future use cases.
- Create a new implementation only when reuse would make the existing design
  unclear, overly coupled, or incompatible with its current contract.
- Keep public APIs small, predictable, and well named.
- Prefer composition over inheritance unless the existing library design already
  uses inheritance for an explicit extension point.
- Avoid framework leakage into domain code whenever practical.
- Do not refactor unrelated code while implementing a focused change.
- Preserve existing module structure and conventions unless the change is
  explicitly about improving that structure.

## Domain-First Design

- Model business rules in the domain first, then adapt them to frameworks,
  persistence, messaging, HTTP, or Spring.
- Keep domain concepts explicit in names, tests, and package structure.
- Separate domain behavior from infrastructure concerns.
- Prefer domain services, value objects, policies, and use-case-oriented APIs
  over anemic procedural flows when real business behavior exists.
- Integration libraries may remain framework-oriented, but test scenarios should
  still describe behavior in domain language.
- Public documentation should focus on module usage and business-facing
  capability explanations. Architecture rules like domain-first design, module
  boundaries, and testing strategy belong in `AGENTS.md`, not in public docs.

### Layering Guidance

- Domain: business rules, decisions, policies, value objects.
- Application service: use-case orchestration.
- Adapter: HTTP, Kafka, Redis, database, Keycloak, SSE, Eureka.
- Library contract: stable reusable API used across modules.

Controllers and framework adapters should translate input and delegate. They
should not own business policy.

## Module Boundary Policy

- For a three-module library, use `common` for stable, transport-neutral public
  contracts, `core` for implementation, and `api` as the consumer-facing
  composition root.
- Keep the dependency direction explicit: `api -> core`, `api -> common`, and
  `core -> common`. `core` must never depend on its own `api`, and `common`
  must never depend on its own `api` or `core`.
- A `common` module may depend only on `lib:common` or another library's
  `common` module. It must not contain Spring, Kafka, HTTP, persistence, or
  another transport implementation.
- A `core` module must not depend on another library's `core`. It consumes a
  stable `api` or `common` contract; concrete backends are assembled by the
  consumer-facing `api` module.
- Public extension classes may be implemented in `core`, but `api` must export
  them when downstream services are expected to extend or reference them.
- Consumers use the library's `api` module. They use `common` directly only to
  define a shared contract without installing the implementation.
- Focused single-module libraries are allowed when splitting them would create
  empty layers. Existing two-module libraries keep their current contract until
  a dedicated migration introduces a real `common` contract and composition
  root; do not silently reverse their dependencies in an unrelated change.
- If two modules could own a change, choose the module closest to the stable
  contract.
- Put framework adapters in `core` modules and reusable neutral contracts in
  `common` modules.
- Do not add module-local abstractions that duplicate shared contracts.

### Mandatory library structure workflow

Before changing a module under `lib/`, write a short responsibility map for
the affected family:

```text
common: public neutral contracts
core: implementation and technical adapters
api: consumer entry point, configuration, and exported extension surface
```

Then:

1. Search production and test imports before moving a public type.
2. Decide whether the type is a neutral contract, implementation, composition
   configuration, or intentional public extension point.
3. Update Gradle dependencies before production code so the intended direction
   is visible in the module graph.
4. Add or update a consumer-boundary test in `api`; it must depend only on the
   public module a real service uses.
5. Run `./gradlew :lib:verifyLibraryStructure` before module tests. This task is
   also part of every library `check` task and rejects reversed dependencies,
   implementation-to-implementation leakage, and production cycles.
6. Run the narrowest affected tests, then the complete `check` task. A change
   is not complete while either verification is red.

## Language Policy

- Business domain code must be written in Kotlin by default.
- Use Java for Spring integration libraries, low-level framework adapters,
  interoperability-heavy APIs, or code that must closely match existing Java
  modules.
- If Java is chosen for business-domain behavior, document the reason in the
  change or in nearby documentation.
- Keep Kotlin domain code idiomatic: immutable data where possible, explicit
  types at module boundaries, and clear nullability.
- Keep Java integration code idiomatic: clear interfaces, small classes, and
  constructor injection.

## Testing Policy

- Treat tests as executable business and integration contracts. Use TDD in the
  order red -> green -> refactor: write the smallest failing behavior test,
  implement the minimum, then simplify the design.
- Assert observable behavior through public boundaries. Do not test private
  methods, fields, internal collections, implementation classes, or incidental
  collaborator call counts.
- Use short outcome-oriented names such as
  `subscribedClientReceivesItsClaimUpdate`, `invalidIdsReturnBadRequest`, and
  `eventIsPublishedAfterCommit`. Avoid `shouldCorrectlyHandle...` and names
  copied from method or class names.
- Prefer Arrange-Act-Assert or Given-When-Then consistently. Keep setup visible
  and use focused domain helpers for plumbing only.
- Mirror production packages in test sources. Use these levels only when they
  represent a real boundary:

  ```text
  src/test/java/.../domain/          # pure rules, no Spring or transports
  src/test/java/.../application/     # use cases with real objects and named fakes
  src/test/java/.../web/             # HTTP contract and response behavior
  src/test/java/.../event/           # event routing and commit/rollback behavior
  src/test/java/.../integration/     # real external boundary
  src/test/java/.../support/         # test app, clients, fakes, containers
  ```

- In `core`, test domain/application behavior and the owned technical boundary
  only. For the stream module this means core tests cover SSE subscriptions,
  routing, limits, cleanup, and emitted SSE behavior; they do not use HTTP
  controllers, Kafka, or event publishers/listeners.
- In `api`, test the HTTP and event boundaries with real Spring wiring. Use
  `@SpringBootTest` plus MockMvc or a real HTTP client, real application
  services, the real `ClientStreamRefreshListener`, and a real transaction
  boundary. Do not use `@WebMvcTest` with mocked services or
  `MockMvcBuilders.standaloneSetup` with Mockito.
- Do not use Mockito, `@Mock`, `@MockBean`, or equivalent doubles as the default
  in API, infrastructure, HTTP, SSE, event, messaging, persistence, or cache
  tests. A mock can make a broken integration appear correct. Use real beans or
  a named deterministic fake at a stable application port instead.
- Use the project’s `test:*` modules and Testcontainers for external services
  whose behavior matters: PostgreSQL, Redis, Kafka, Elasticsearch, Keycloak,
  Eureka, or another real dependency. Verify serialization, connectivity,
  retries, transactions, and routing against the running service.
- Do not add a fake container just to satisfy a rule. The stream event path
  uses a service-scoped Kafka topic and a pod-specific consumer group, so its
  API test must use real Spring wiring, a real transaction boundary, and the
  existing `test:kafka` Testcontainers module. Verify the HTTP -> event
  publisher -> Kafka -> listener -> SSE behavior.
- Do not use arbitrary sleeps. Use the project Awaitility helpers or wait for
  an observable readiness/event condition.
- Keep container setup, test applications, HTTP/SSE clients, and reusable fakes
  in `support` or an existing `test:*` module. Scenario tests should contain
  only domain setup, the public action, and observable assertions.
- Before finishing, run the narrowest relevant test task, then a broader check
  when practical. Never report tests as passing when they were not executed;
  report missing Docker, Gradle, container, or network prerequisites exactly.

## Project Documentation

Public documentation is maintained as Markdown in the central Docusaurus site.

Project-specific locations:

```text
docs/                     Public Markdown documentation.
docs/libraries/           Production library guides.
docs/test-modules/        Reusable test-support module guides.
docs/getting-started/     Project orientation and module ownership.
docs-site/                Docusaurus configuration and static-site output.
docs-site/sidebars.js     Public documentation navigation.
lib/                      Production libraries documented by the site.
test/                      Reusable test-support modules documented by the site.
```

Project-specific documentation rules:

- Public behavior is documented in the central `docs/` site. Module-local
  README files are secondary entry points.
- Production guides belong under `docs/libraries/`; reusable test-module guides
  belong under `docs/test-modules/`.
- Add or remove corresponding entries in `docs-site/sidebars.js` when pages
  change.
- Module guides should cover purpose, use cases, module boundaries, setup,
  usage, extension points, operational notes, and related documentation.
- Use verified Gradle module paths, package names, properties, endpoints, bean
  names, and event names. Do not invent APIs or implementation behavior.
- Use Mermaid code blocks for architecture, dependency, lifecycle, and event
  diagrams. The Docusaurus site is configured to render them.

Documentation tooling:

```text
./gradlew doc-build
./gradlew doc-dev
.\\gradlew.bat doc-build
.\\gradlew.bat doc-dev
```

The local documentation server is available at `http://127.0.0.1:3000`.
Documentation tooling runs through Docker or Podman; a local Python installation
is not required. `doc-build` writes the static site to `docs-site/build/`.
`doc-dev` serves the site from a container and rebuilds pages while Markdown
files change. The container image is defined in `docs-site/Dockerfile`.

Keep documentation operational details in `AGENTS.md`; public pages should
focus on module purpose, architecture decisions, examples, and usage.

## Spring And Integration Libraries

- Keep Spring configuration focused and explicit.
- Prefer constructor injection.
- Avoid hiding important behavior in broad component scanning when a focused
  configuration class would be clearer.
- Keep adapter code thin: translate framework input into domain or library
  calls, then delegate.
- Avoid coupling reusable APIs to Spring unless the module is explicitly a Spring
  integration module.

## Change Workflow

- Read the relevant module and tests before editing.
- Make the smallest coherent change that satisfies the task.
- Add or update tests at the right level.
- Run the narrowest relevant test task first, then broader checks when the
  change affects shared behavior.
- For documentation changes, run `.\gradlew.bat doc-build` when container tooling
  is available. If it cannot be run, report the exact missing runtime or failure.
- Report any verification that could not be run and the exact reason.

## Review Checklist

Before finishing a change, verify:

- The domain language is visible in names and tests.
- Infrastructure details are isolated from domain behavior.
- Tests are readable and avoid duplicated plumbing.
- The implementation follows the existing module conventions.


## Logging Boundary

- Reusable libraries must depend on lib:logger:api for logging contracts.
- Concrete logging backends belong in lib:logger:core and must be wired explicitly
  through configuration.
- Do not import SLF4J, Logback, or Commons Logging directly into reusable domain,
  application, Stream, or Event code.
- Log technical context and failures without business payloads, tokens,
  authorization headers, or full event objects.
- A new log statement is part of the public operational behavior: update the
  relevant library documentation when it changes troubleshooting or production
  configuration.

## Stream/Event Boundary

- stream:common contains the public refresh event contract and transport-neutral integration errors; it must not contain Spring, HTTP, Kafka, or SSE implementation details.
- stream:core owns SSE subscriptions, routing, limits, queues, and notification
  serialization; these implementation classes are not public library contracts.
- stream:api exposes the HTTP controller and the event entry point needed by
  consuming applications. Kafka topic and consumer-group mechanics remain internal.
- A Stream refresh is a notification containing resource name, resource ID, and
  source version; it must not carry a business payload.
- Stream refresh events are service-scoped Kafka events. Every pod of one service
  uses its own consumer group; other services and other event types must keep
  their existing routing.
