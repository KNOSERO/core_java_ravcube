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

- Use an `api` module for contracts that other modules should depend on.
- Use a `core` module for concrete Spring, Redis, Kafka, Elasticsearch,
  Keycloak, Eureka, HTTP, or SSE implementation.
- If two modules could own a change, choose the module closest to the stable
  contract.
- Put framework adapters in `core` modules and reusable contracts in `api`
  modules.
- Do not add module-local abstractions that duplicate shared contracts.

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
  routing, authorization, limits, cleanup, and emitted SSE behavior; they do
  not use HTTP controllers or event publishers/listeners.
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
- Do not add a fake container just to satisfy a rule. The current stream event
  path uses the in-process Spring `AFTER_COMMIT` transport, so its API test must
  use real Spring event wiring and commit/rollback behavior. If the event path
  later changes to Kafka or another external transport, add the corresponding
  Testcontainers integration test and reuse its existing `test:*` module.
- Do not use arbitrary sleeps. Use the project Awaitility helpers or wait for
  an observable readiness/event condition.
- Keep container setup, test applications, HTTP/SSE clients, and reusable fakes
  in `support` or an existing `test:*` module. Scenario tests should contain
  only domain setup, the public action, and observable assertions.
- Before finishing, run the narrowest relevant test task, then a broader check
  when practical. Never report tests as passing when they were not executed;
  report missing Docker, Gradle, container, or network prerequisites exactly.

## Documentation Policy

Documentation is part of the product. Every public behavior, integration point,
configuration option, test utility, and reusable module should be understandable
from the Markdown documentation without reading implementation code first.

Write documentation for library consumers and future maintainers:

- Explain what the module does, when to use it, and when not to use it.
- Describe the public contract, observable behavior, configuration, extension
  points, and integration boundaries.
- Use real names from the repository: Gradle module paths, package names, class
  names, annotations, profiles, properties, endpoints, topics, and bean names.
- Prefer short, concrete examples over abstract prose.
- Document assumptions and failure modes when they affect correct usage, such as
  required Spring profiles, container dependencies, transaction timing, Kafka
  routing, Redis configuration, Keycloak setup, retry behavior, or idempotency.
- Keep pages stable for AI retrieval: use durable headings, descriptive page
  names, explicit module paths, and links to related documentation pages.
- Do not document private implementation details unless they directly affect how
  users integrate the library.
- Do not put agent workflow rules, architecture policy, or build-operation notes
  into public documentation pages. Keep those in `AGENTS.md`.

Apply engineering principles to documentation:

- KISS: start with the simplest correct mental model. Add detail only when it
  helps the reader make a correct decision.
- DRY: avoid repeating long setup instructions or explanations across pages.
  Link to shared documentation when the same concept applies to multiple
  modules, but keep short module-specific notes where they prevent mistakes.
- SOLID-style responsibility: one page should have one clear purpose. A module
  guide should document that module, shared concepts should live in shared
  guides, and unrelated capabilities should be split into separate pages.
- Consistency: use the same words for the same domain concept across code,
  tests, and documentation.

Keep public documentation in the central `docs/` site so it is visible in the
Docusaurus navigation. Module-local README files may exist as secondary entry
points, but public behavior should also be represented from the central site.

### Documentation Update Requirements

For every code change, explicitly decide whether documentation is affected.
Update documentation when any of the following changes:

- A public class, interface, annotation, configuration property, Spring profile,
  bean, endpoint, event, repository contract, or test helper is added, renamed,
  removed, or changes behavior.
- A module dependency, supported technology, external service assumption,
  profile, property, container requirement, or Gradle task changes.
- A guide example no longer matches the real API.
- A new module or documentation page is added.

When documentation is affected:

- Update the matching guide under `docs/libraries/` for production modules or
  `docs/test-modules/` for reusable test modules.
- Update `docs/getting-started/project-map.md` when module ownership, purpose, or
  use cases change.
- Update `docs/index.md` and `docs-site/sidebars.js` when adding or removing a
  page that should appear in navigation.
- Keep examples conceptually compilable and aligned with actual Gradle module
  names from `settings.gradle.kts`.

### Professional Guide Structure

Use this structure for new or substantially rewritten module guides unless the
existing page has a clearer local convention:

1. Module purpose: one short paragraph explaining the capability and the problem
   it solves.
2. When to use it: concise scenarios that help users choose the module.
3. Module boundaries: what the module owns, what it delegates to other modules,
   and whether it is a contract module, implementation module, or test-support
   module.
4. Setup: Gradle module dependency, Spring configuration, profiles, and external
   services required for runtime or tests.
5. Usage example: a small Java or Kotlin example using real API names.
6. Extension points: interfaces, annotations, configuration classes, listeners,
   publishers, repositories, or hooks users are expected to implement.
7. Operational notes: transactions, retries, caching behavior, serialization,
   container startup, ports, credentials, or other runtime constraints.
8. Related documentation: links to nearby guides that explain connected modules
   or shared concepts.

Keep pages short enough to scan. If a page grows into unrelated topics, split it
and add both pages to the Docusaurus sidebar.

### Writing Standard

Documentation should be precise, practical, and professional:

- Use direct language and short paragraphs.
- Prefer active voice: "Configure the Redis profile" instead of "The Redis
  profile should be configured."
- Start sections with the outcome the reader needs, then provide the mechanics.
- Use tables for comparison and module maps, but use prose for behavior and
  trade-offs.
- Use fenced code blocks with language tags for examples.
- Keep examples small enough to understand, but complete enough to show the real
  integration point.
- Name prerequisites explicitly before examples that depend on Spring profiles,
  containers, security realms, topics, ports, or external services.
- Avoid marketing language, vague claims, and placeholder names.
- Avoid documenting obvious code behavior that the public names already explain.
- Do not invent APIs, properties, modules, or behavior. Inspect the code first.

## Documentation Builder Direction

The preferred documentation output is a complete static website built from
Markdown guides. Use Docusaurus for the public documentation site.

When adding or changing a public feature:

- Update or create Markdown documentation for user-visible behavior.
- Include examples that compile conceptually and match the actual API names.
- Document module boundaries and integration points.
- Prefer stable page names and links so Markdown documentation remains useful for
  AI context retrieval.
- Place cross-module documentation under `docs/`.
- Place module-specific guides under `docs/libraries/` or `docs/test-modules/`
  so the documentation remains visible in the main navigation.

Documentation locations:

```text
docs/                     Public Markdown documentation.
docs/libraries/           Production library guides.
docs/test-modules/        Reusable test-support module guides.
docs/getting-started/     Project orientation and module ownership.
docs-site/                Docusaurus site configuration and static-site output.
docs-site/sidebars.js     Navigation for public documentation pages.
lib/                      Production libraries that should be documented.
test/                     Reusable test-support modules that should be documented.
```

Current build flow:

```text
./gradlew doc-build
./gradlew doc-dev
```

Current local documentation tooling:

```powershell
.\gradlew.bat doc-build
.\gradlew.bat doc-dev
```

The local documentation server exposes the project documentation at:

```text
http://127.0.0.1:3000
```

Documentation tooling must run through container-managed Gradle tasks. Use
Docker or Podman automatically through `doc-build` and `doc-dev`. Do not require
a local Python installation for building or serving docs.

Documentation operational details belong in this file, not in user-facing
documentation pages. Public documentation should focus on module purpose,
architecture decisions, examples, and usage.

Documentation runtime notes for agents:

- `doc-build` builds the complete static documentation site into
  `docs-site/build/`.
- `doc-dev` serves Docusaurus from a container on `http://127.0.0.1:3000` and
  rebuilds pages while documentation files change.
- `doc-dev` stops the previous documentation container for the selected port
  before starting a new one.
- The container image is defined in `docs-site/Dockerfile`.
- Docusaurus dependencies are installed under `/opt/docs-runtime` so repository
  mounts do not hide `node_modules`.
- The serve task sets `BROWSER=none` to prevent Docusaurus from trying to open
  a browser from inside Linux containers.
- Podman on Windows may require `podman machine set --user-mode-networking=true`.

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
- Read the relevant documentation page before editing public behavior. If no
  relevant page exists, create one under the appropriate documentation section.
- Make the smallest coherent change that satisfies the task.
- Add or update tests at the right level.
- Update documentation for public behavior changes in the same change.
- Run the narrowest relevant test task first, then broader checks when the
  change affects shared behavior.
- For documentation changes, run `.\gradlew.bat doc-build` when container tooling
  is available. If it cannot be run, report the exact missing runtime or failure.
- Report any verification that could not be run and the exact reason.

## Review Checklist

Before finishing a change, verify:

- The domain language is visible in names and tests.
- Infrastructure details are isolated from domain behavior.
- Public behavior is documented in the relevant Markdown guide.
- Tests are readable and avoid duplicated plumbing.
- Documentation is updated when behavior or usage changed.
- The implementation follows the existing module conventions.
