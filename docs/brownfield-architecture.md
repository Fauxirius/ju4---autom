# powermock-api Brownfield Architecture Document

## Introduction

This document captures the CURRENT STATE of the `powermock-api` codebase. It is intended as a reference for AI agents to understand the project's structure, conventions, and the significant discrepancy between its stated goal and its implementation.

### Document Scope

This is a comprehensive documentation of the entire system, as requested.

### Change Log

| Date       | Version | Description                 | Author    |
| ---------- | ------- | --------------------------- | --------- |
| 2025-12-15 | 1.0     | Initial brownfield analysis | Architect |

## Quick Reference - Key Files and Entry Points

### Critical Files for Understanding the System

-   **Main Entry**: `src/main/java/com/javatechie/pm/api/PowermockApiApplication.java`
-   **Core Business Logic**: `src/main/java/com/javatechie/pm/api/service/OrderService.java`
-   **API Definitions**: Defined in `PowermockApiApplication.java` (`/placeOrder`).
-   **Data Models**: `src/main/java/com/javatechie/pm/api/dto/OrderRequest.java`, `src/main/java/com/javatechie/pm/api/dto/OrderResponse.java`
-   **Build Configuration**: `pom.xml`
-   **Testing**: `src/test/java/com/javatechie/pm/api/PowermockApiApplicationTests.java`

## High Level Architecture

### Technical Summary

This project is a simple monolithic Spring Boot application using Java 17. It exposes a single REST endpoint (`/placeOrder`) to simulate checking out an order. The service layer contains a private method for applying a discount, which is a candidate for advanced mocking techniques.

The project's stated purpose in `README.md` is to demonstrate mocking of final, static, or private methods using **PowerMock**. However, the actual implementation **does not contain PowerMock**. The single test uses standard **Mockito** to mock a utility class and does not test the private method logic at all.

### Actual Tech Stack (from pom.xml)

| Category             | Technology            | Version      | Notes                                  |
| -------------------- | --------------------- | ------------ | -------------------------------------- |
| Language             | Java                  | 17           | Defined in `pom.xml`                   |
| Runtime              | Spring Boot           | 3.2.5        | Parent POM                             |
| Framework            | Spring Web            | 3.2.5        | For RESTful services                   |
| Development Tools    | Spring Boot DevTools  | 3.2.5        | For live reloading                     |
| Code Generation      | Lombok                | (managed)    | For reducing boilerplate code          |
| Testing              | Spring Boot Starter Test | 3.2.5        | Includes JUnit 5 and Mockito           |

### Repository Structure Reality Check

-   **Type**: Polyrepo (single, self-contained project)
-   **Package Manager**: Maven
-   **Notable**: Standard Maven project structure.

## Source Tree and Module Organization

### Project Structure (Actual)

<img width="1280" height="720" alt="image" src="C:\Users\DeepakYadav\Desktop\Demo\JU4 - test2\nanobanana-output\Architecture diagram.png" />

```text
powermock-api/
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── javatechie/
    │               └── pm/
    │                   └── api/
    │                       ├── dto/
    │                       │   ├── OrderRequest.java
    │                       │   └── OrderResponse.java
    │                       ├── service/
    │                       │   └── OrderService.java
    │                       ├── util/
    │                       │   └── NotificationUtil.java
    │                       └── PowermockApiApplication.java
    └── test/
        └── java/
            └── com/
                └── javatechie/
                    └── pm/
                        └── api/
                            └── PowermockApiApplicationTests.java
```

### Key Modules and Their Purpose

-   **PowermockApiApplication**: The main Spring Boot application class. It also acts as the `RestController` for the `/placeOrder` endpoint.
-   **OrderService**: Contains the core business logic for processing an order. It includes a private method `addDiscount` which is central to the project's supposed demonstration purpose.
-   **NotificationUtil**: A stubbed service meant to simulate sending an email notification.
-   **DTOs (`OrderRequest`, `OrderResponse`)**: Data Transfer Objects used for the API request and response.

## Data Models and APIs

### Data Models

The data models are defined as Java records in the `dto` package.

-   **OrderRequest**: See `src/main/java/com/javatechie/pm/api/dto/OrderRequest.java`
-   **OrderResponse**: See `src/main/java/com/javatechie/pm/api/dto/OrderResponse.java`

### API Specifications

-   **Endpoint**: `POST /placeOrder`
-   **Controller**: `PowermockApiApplication.java`
-   **Request Body**: `OrderRequest` JSON object.
-   **Response Body**: `OrderResponse` JSON object.
-   **Purpose**: Simulates placing an order and returns a response message.

## Technical Debt and Known Issues

### Critical Technical Debt

1.  **Project Fails its Stated Purpose**: The most significant issue is that the project does not fulfill the goal described in `README.md`. It claims to be a demonstration of **PowerMock** for testing private methods, but **it does not use PowerMock**.
2.  **Incomplete Testing**: The existing test, `PowermockApiApplicationTests.java`, uses basic Mockito to verify a call to a utility class. It **does not test the private `addDiscount` method's logic**, which is the primary challenge this project was supposedly created to solve. The project is a "how-to" that doesn't "how-to".

## Integration Points and External Dependencies

### External Services

-   None. The `NotificationUtil` class simulates an external integration (e.g., a mail API) but is currently a hardcoded stub returning "success".

### Internal Integration Points

-   The `PowermockApiApplication` controller class directly injects and calls the `OrderService`.
-   The `OrderService` depends on and calls `NotificationUtil`.

## Development and Deployment

### Local Development Setup

1.  Ensure Java 17 and Maven are installed.
2.  Run `mvn spring-boot:run` from the project root.
3.  The application will start on the default port (likely 8080).

### Build and Deployment Process

-   **Build Command**: `mvn clean package`
-   **Output**: A JAR file is created in the `target/` directory (e.g., `powermock-api-0.0.1-SNAPSHOT.jar`).
-   **Deployment**: The application is run using `java -jar target/powermock-api-0.0.1-SNAPSHOT.jar`.

## Testing Reality

### Current Test Coverage

-   **Unit Tests**: Coverage is minimal. The single existing test case verifies that `notificationUtil.sendEmail()` is called. It does not validate any business logic, calculations from the private `addDiscount` method, or error conditions.
-   **Integration/E2E Tests**: None.

### Running Tests

```bash
mvn test
```

## Appendix - Useful Commands and Scripts

### Frequently Used Commands

```bash
# Run the application in development mode
mvn spring-boot:run

# Run the unit tests
mvn test

# Build the application into a JAR file
mvn clean package
```
