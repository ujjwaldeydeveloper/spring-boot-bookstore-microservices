# Bookstore Microservices — Learning Notes

These notes follow the project commit by commit. Each entry records what was added and the main lesson learned.

## 1. `5dd005b` — Create the initial Java project structure

### What changed

- Created the repository and IntelliJ project structure.
- Added `.gitignore` rules for IDE files, build output, and operating-system files.
- Configured the project to use Java 21.

### What I learned

- A project should begin with a clean source-control boundary.
- Generated files, local IDE state, and build output should not be committed.
- Choosing the Java version early prevents differences between developer machines.

## 2. `aca670a` — Add SDKMAN, the parent POM, and Maven Wrapper

### What changed

- Added `.sdkmanrc` with Java 21 and Maven 3.9.16.
- Added the Maven Wrapper.
- Created a root POM with `pom` packaging for a multi-module project.

### What I learned

- SDKMAN keeps the local Java and Maven versions consistent.
- The Maven Wrapper lets developers and CI use the project's Maven version through `./mvnw`.
- A parent project with `<packaging>pom</packaging>` can act as an aggregator for multiple services.
- An aggregator only builds modules declared in its `<modules>` section.

## 3. `5815cfa` — Add `catalog-service` as a Maven module

### What changed

- Added the first Spring Boot service.
- Registered `catalog-service` in the root POM.
- Added JPA, Flyway, validation, web, Actuator, PostgreSQL, and Testcontainers dependencies.
- Added a Testcontainers configuration for PostgreSQL 16.

### What I learned

- Every microservice can be an independently buildable Maven module.
- The root build can run all modules, while `./mvnw -pl catalog-service ...` targets one module.
- `@ServiceConnection` allows Spring Boot to obtain database connection details directly from a Testcontainer.
- Testcontainers gives tests a real PostgreSQL database without requiring a permanently running test database.

## 4. `831091e` — Expose build and Git information through Actuator

### What changed

- Exposed the Actuator `health` and `info` endpoints.
- Moved management endpoints to port `8081`.
- Added Spring Boot build information.
- Added Git branch, commit ID, author, and message information.
- Enabled graceful application shutdown.

### What I learned

- Actuator provides operational information about a running service.
- A separate management port isolates operational endpoints from the public application API.
- Build and Git metadata make it possible to identify exactly which version is deployed.
- Only required Actuator endpoints should be exposed.
- Graceful shutdown lets active requests finish before the application stops.

## 5. `1c5f088` — Add OpenAPI and Swagger UI

### What changed

- Added Springdoc OpenAPI support.
- Exposed OpenAPI and Swagger UI on the management port.

### What I learned

- OpenAPI is a machine-readable description of the HTTP API.
- Swagger UI renders that description as interactive documentation.
- Keeping documentation on the management port can avoid adding operational endpoints to the public API port.
- API documentation is most useful when it stays generated from the actual application endpoints.

## 6. `d490cfe` — Add REST Assured

### What changed

- Added REST Assured as a test dependency.

### What I learned

- REST Assured provides a readable `given`/`when`/`then` DSL for testing HTTP APIs.
- It can verify the response status, content type, headers, and JSON body.
- Calling the running application over HTTP tests more of the real request path than directly invoking a controller method.

## 7. `89b87ae` — Add Spotless formatting

### What changed

- Added the Spotless Maven plugin.
- Added import ordering, unused-import removal, annotation formatting, and Java source formatting.

### What I learned

- A formatter should be the single source of truth for code style.
- Formatting in the Maven build keeps local development and CI consistent.
- In a multi-module build, `spotless:apply` must run in a module where the plugin is configured, for example:

  ```shell
  ./mvnw -pl catalog-service spotless:apply
  ```

- Maven cannot resolve the `spotless` prefix from the root aggregator when the plugin exists only in `catalog-service`.
- Formatter output is based on syntax and configured line-wrapping rules, not on making every record visually identical.
- An Eclipse formatter profile gives control over line width and record wrapping without `spotless:off` comments.

## 8. `40b95e2` — Add PostgreSQL with Docker Compose

### What changed

- Added a PostgreSQL 16 Alpine container for local development.
- Mapped host port `15432` to container port `5432`.
- Added a database health check and memory limit.
- Made database URL, username, and password configurable through environment variables.

### What I learned

- Docker Compose makes local infrastructure reproducible.
- Port mapping allows the application on the host to connect to a database inside a container.
- Environment-variable overrides keep configuration flexible across local, CI, and deployment environments.
- A health check reports readiness; a started container is not necessarily ready to accept connections.
- The development Compose database and a Testcontainers database serve different purposes.

## 9. `1409f5f` — Add GitHub Actions for `catalog-service`

### What changed

- Added a CI workflow that runs for changes under `catalog-service`.
- Configured Temurin Java 21 and Maven dependency caching.
- Ran `./mvnw verify` in CI.

### What I learned

- CI verifies that the project builds and tests successfully outside the developer's machine.
- Path filters avoid running a service workflow when unrelated files change.
- Maven caching speeds up builds but does not replace dependency declarations or the Maven Wrapper.
- `verify` runs the Maven lifecycle through compilation, tests, packaging, and verification checks.

## 10. `ba82b65` — Add Task as a command runner

### What changed

- Added tasks for formatting, testing, building images, and managing Docker Compose services.
- Added small operating-system-specific command variables.

### What I learned

- A Taskfile gives the team short, memorable commands for longer workflows.
- Tasks can depend on other tasks; for example, tests can require formatting first.
- Centralizing commands reduces differences between documentation, local usage, and CI.
- Task is an orchestration layer; Maven still owns the Java build, and Docker Compose still owns containers.

## 11. `1fef6bf` — Add Flyway database migrations

### What changed

- Added a versioned migration to create the product sequence and table.
- Added a second migration containing initial book data.

### What I learned

- Flyway applies migrations in version order and records which migrations have run.
- Schema changes should be versioned and committed with the application code.
- Existing migrations should normally remain immutable after they have been applied; new changes belong in a new migration.
- Database constraints such as primary keys, unique product codes, and non-null columns protect data independently of Java validation.
- PostgreSQL sequences can generate entity identifiers.

## 12. `507aa41` — Implement the paginated Products API

### What changed

- Added `ProductEntity`, `ProductRepository`, `ProductService`, and `ProductController`.
- Added domain response records: `Product` and `PageResult<T>`.
- Added entity-to-domain mapping.
- Added `GET /api/products?page=1`.
- Made the page size configurable with a default of 10 and a minimum value of 1.
- Sorted products by name.

### What I learned

- The controller handles HTTP concerns, the service contains use-case logic, and the repository handles persistence.
- A JPA entity represents the database model; an API/domain record represents data exposed outside persistence.
- Mapping prevents persistence details such as an internal database ID from leaking into the API.
- Spring Data's `JpaRepository` supplies common operations such as `findAll()` without an implementation class.
- A derived query is a custom repository method such as `findByCode(String code)` whose query Spring derives from the method name.
- Spring Data page numbers are zero-based, while a public API can expose one-based page numbers by converting at the boundary.
- `Page<T>` provides content, total elements, total pages, and navigation state.
- `@ConfigurationProperties` creates type-safe configuration, while `@Validated` and `@Min(1)` reject invalid page sizes.
- Java records are useful for immutable data carriers.

## 13. `ef36c27` — Add an API integration test

### What changed

- Added a reusable `AbstractIT` base class.
- Started the application on a random port.
- Configured REST Assured with that port.
- Added deterministic SQL test data containing 14 products.
- Tested the Products API response and pagination metadata.
- Scoped the format task to `catalog-service`.

### What I learned

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` starts the real web application on an available port.
- `@LocalServerPort` provides the selected port to the test.
- An integration test checks the complete path from HTTP through the controller, service, repository, and database.
- `@Sql` prepares known data before a test so assertions remain deterministic.
- PostgreSQL's `generate_series` can create compact, repeatable fixture data.
- `delete from products` in `test-data.sql` affects only the test database selected by the test configuration, not the normal development database.
- Resetting fixture data before each test prevents one test's changes from changing another test's expected result.

## 14. `b310df8` — Add a repository slice test

### What changed

- Added `ProductRepositoryTest` with `@DataJpaTest`.
- Used a Testcontainers JDBC URL for PostgreSQL 16.
- Loaded the same SQL fixture and verified that 14 products are returned.

### What I learned

- A slice test loads only the part of the Spring application needed for the component under test.
- `@DataJpaTest` focuses on JPA entities, repositories, and database behavior and is faster and narrower than a full HTTP integration test.
- `spring.test.database.replace=none` prevents Spring from replacing PostgreSQL with an embedded database.
- A `jdbc:tc:postgresql:16-alpine:///db` URL starts a temporary PostgreSQL container through the Testcontainers JDBC driver.
- The database type in a Testcontainers JDBC URL must be lowercase: `postgresql`, not `PostgreSQL`.
- Test assertions must come from the intended assertion library; here, JUnit's `assertEquals` checks the collection size.
- `@DataJpaTest` is transactional by default, so changes made by each test are rolled back afterward.

## 15. 74bdf80c — Add product lookup by code and API error handling

### What changed

- Added the derived repository query `findByCode(String code)`.
- Added `ProductService.getProductByCode` and mapped `ProductEntity` to the public `Product` record.
- Added `GET /api/products/{code}`.
- Added `ProductNotFoundException` for a missing product.
- Added global exception handling with Spring's `ProblemDetail` response type.
- Added repository tests for existing and missing product codes.
- Added controller integration tests for successful and missing-product responses.
- Deserialized the successful HTTP response into a `Product` and compared it with AssertJ.

### What I learned

- Spring Data derives a query from `findByCode` because `code` is a field on `ProductEntity`.
- A repository method declared as `Optional<ProductEntity>` returns `Optional.empty()` when no row matches. It should not be compared with `""`, because an empty string is still a `String`, not an empty `Optional`.
- Equality assertions work with an empty result when both sides have the same type:

  ```java
  assertEquals(Optional.empty(), repository.findByCode("missing-code"));
  ```

- `optional.isEmpty()` expresses the same absence check directly. `assertEquals(Optional.empty(), actual)` additionally makes the expected container value explicit.
- `Optional.map(...)` transforms a present `ProductEntity` into a `Product` while preserving absence, so no manual `null` check is required.
- The controller can turn a present value into `ResponseEntity.ok(...)` and use `orElseThrow(...)` to move the missing-value path into centralized exception handling.
- A domain-specific exception communicates the failure more clearly than throwing a generic exception. Factory methods such as `forCode(code)` centralize exception-message creation.
- Exception messages assembled from multiple strings need deliberate spaces and punctuation so the resulting API detail remains readable.
- `@RestControllerAdvice` and `@ExceptionHandler` apply consistent error handling across controllers.
- `ProblemDetail` provides standard fields such as `type`, `title`, `status`, and `detail`; additional fields such as `service`, `error_category`, and `timestamp` can provide operational context.
- A not-found integration test should verify the HTTP `404` status and the Problem Details contract, not only that Java threw an exception.
- A REST Assured path placeholder must receive a value. Both forms below bind `code` correctly:

  ```java
  get("/api/products/{code}", code);
  given().pathParam("code", code).get("/api/products/{code}");
  ```

- Leaving `{code}` unbound causes REST Assured to fail before the request reaches Spring with `Invalid number of path parameters`.
- REST Assured can deserialize JSON using `.extract().as(Product.class)`, which makes domain-level response assertions readable.
- AssertJ's `assertThat` must be imported from `org.assertj.core.api.Assertions`; an IDE may suggest unrelated methods with the same name from other libraries.
- Java records implement value-based `equals`, so an actual `Product` can be compared directly with an expected `Product`:

  ```java
  assertThat(actualProduct).isEqualTo(expectedProduct);
  ```

- `BigDecimal.equals` compares both numeric value and scale, so test fixtures should construct the expected price in the same representation returned by JSON, for example `new BigDecimal("1")`.
- Repository slice tests and HTTP integration tests complement each other: the slice test verifies persistence behavior, while the integration test verifies routing, serialization, service mapping, exception handling, and the database together.

# Architecture Notes

## Package by layer

Code is grouped by technical responsibility:

```text
controller/
service/
repository/
entity/
```

This is simple for small applications, but one feature becomes spread across several top-level packages.

## Package by feature

Code is grouped by business capability:

```text
product/
  ProductController
  ProductService
  ProductRepository
customer/
  CustomerController
  CustomerService
  CustomerRepository
```

This keeps related code together and usually scales better as more features are added.

## Package by component

Each component exposes a small public API and keeps its implementation details package-private where possible. Other components call the service or facade rather than reaching directly into its repository.

The current catalog code follows this idea in part: `ProductService` is public, while `ProductRepository`, `ProductEntity`, and `ProductMapper` remain package-private.

# Testing Notes

## Integration test

- Starts most or all of the application.
- Makes a real HTTP request.
- Checks how multiple layers work together.
- In this project: `ProductControllerTest`.

## Slice test

- Loads a focused part of the application.
- Gives faster, more precise feedback for one layer.
- In this project: `ProductRepositoryTest` uses the JPA slice.

## Unit test

- Tests one class in isolation.
- Usually does not start Spring or external infrastructure.
- Is useful for service rules and edge cases that do not require framework integration.

## Test database safety

- Local development uses PostgreSQL from Docker Compose.
- Tests use temporary PostgreSQL containers.
- `test-data.sql` resets data only in the database connected to the test.
- Tests should never point their datasource URL at a shared or production database.
