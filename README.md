# service-actor-management

## Overview

**service-actor-management** is a microservice designed to manage actors and their main characteristics within a cinema or entertainment domain. The application provides a reactive REST API for creating, updating, retrieving, and listing actors, supporting modern security and integration standards. It is built with Kotlin and Spring Boot, leverages MongoDB for data persistence, and integrates with Kafka for event-driven communication.

## Functional Summary

- Manage actor entities: create, update, retrieve by ID, and list actors.
- Enforce validation and business rules (e.g., date of birth, uniqueness).
- Secure endpoints with OAuth2 and role-based access control.
- Emit and consume events via Kafka for integration with other services.
- Provide OpenAPI documentation for easy API consumption.
- Support for automated testing and code coverage reporting.

## Technical Specifications

- **Language:** Kotlin (JVM 17)
- **Framework:** Spring Boot 3.5.x (WebFlux, Reactive MongoDB, Security, Actuator)
- **Database:** MongoDB (Reactive)
- **Messaging:** Apache Kafka (with Avro serialization)
- **API Documentation:** OpenAPI 3 (generated via restdocs-api-spec)
- **Validation:** Jakarta Validation (Bean Validation API)
- **Security:** OAuth2 Resource Server (JWT), Spring Security
- **Testing:** JUnit 5, Testcontainers (MongoDB, Kafka, Keycloak), MockK
- **Build Tool:** Gradle (Kotlin DSL)
- **CI/CD:** GitLab CI/CD with SonarQube, JaCoCo, container scanning, and Helm-based deployment
- **Containerization:** Docker (Amazon Corretto 17 Alpine base image)
- **Code Quality:** SonarQube integration, code coverage via JaCoCo
- **Documentation:** OpenAPI spec and REST Docs generated automatically

## Project Structure

- `src/main/kotlin` – Application source code
- `src/main/resources` – Configuration files
- `src/main/avro` – Avro schemas for Kafka events
- `src/test/kotlin` – Test code
- `build.gradle.kts` – Build configuration
- `Dockerfile` – Containerization setup
- `ops/` – Deployment and load testing scripts (Helm charts, k6 scripts)
- `docs/` – Generated API documentation

## Getting Started

1. **Build the application:**
   ```sh
   ./gradlew build
   ```
2. **Run tests and generate coverage:**
   ```sh
   ./gradlew test
   ```
3. **Run locally:**
   ```sh
   ./gradlew bootRun
   ```
4. **Build Docker image:**
   ```sh
   docker build -t service-actor-management .
   ```
5. **Run with Docker:**
   ```sh
   docker run -p 8080:8080 service-actor-management
   ```

## API Documentation

- OpenAPI 3 specification is generated at build time and available in the `docs/` directory.
- REST API follows standard RESTful conventions and supports JSON payloads.

## Security

- All endpoints are secured using OAuth2 (JWT Bearer tokens).
- Role-based access control is enforced for sensitive operations.

## Testing & Quality

- Unit and integration tests use JUnit 5, Testcontainers, and MockK.
- Code coverage is reported via JaCoCo.
- Static code analysis and quality gates are enforced with SonarQube.

## Deployment

- CI/CD pipeline is defined in `.gitlab-ci.yml` for automated build, test, analysis, containerization, and deployment.
- Helm charts are provided for Kubernetes deployment.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
