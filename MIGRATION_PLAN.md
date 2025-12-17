# Migration Plan: Upgrading to Java 17, Spring Boot 3, and JUnit 5

## 1. Objective

This document outlines the comprehensive plan to modernize the `powermock-api` project. The migration has three primary goals:

1.  **Upgrade the Java Development Kit (JDK)** from version 8 to **17 (LTS)**.
2.  **Upgrade the Spring Boot Framework** from the outdated `2.1.1.RELEASE` to a modern version, **`3.2.5`**.
3.  **Modernize the Testing Framework** by migrating from JUnit 4 and PowerMock to **JUnit 5** and standard **Mockito**.

This migration will improve the project's performance, security, and maintainability, while also enabling developers to use modern language and framework features.

## 2. Current Project State

An initial analysis of the `pom.xml` reveals the following:

-   **Java Version**: `1.8`
-   **Spring Boot Version**: `2.1.1.RELEASE` (Unsupported)
-   **Testing Stack**: JUnit 4 with PowerMock and Mockito 2.

The use of PowerMock indicates that the application code likely contains patterns that are difficult to test, such as calls to static methods.

## 3. Target State

-   **Java Version**: `17`
-   **Spring Boot Version**: `3.2.5`
-   **Testing Stack**: JUnit 5 (Jupiter) with standard Mockito (provided by Spring Boot).

## 4. Key Challenges & Strategy

1.  **Challenge**: **Major Spring Boot Upgrade (2.1 → 3.2)**
    -   **Strategy**: This is the most significant part of the migration. We will update the parent POM directly. This requires us to handle breaking changes, most notably the move from `javax.*` to the `jakarta.*` package namespace.

2.  **Challenge**: **Removing PowerMock**
    -   **Strategy**: PowerMock is incompatible with JUnit 5. We will replace it by refactoring the production code to follow modern dependency injection principles. This will make the code cleaner and more testable with standard Mockito.

## 5. Detailed Step-by-Step Migration Plan

---

### **Phase 0: Preparation**

1.  **Create a dedicated branch** to isolate the migration work.
    ```bash
    git checkout -b feature/migration-java17-junit5
    ```
2.  **Confirm the current build is stable.** Run `mvn clean install` on the `main` branch to ensure you are starting from a healthy state.

---

### **Phase 1: Update Build Configuration (`pom.xml`)**

1.  **Update the Java Version Property:**
    ```xml
    <!-- Before -->
    <java.version>1.8</java.version>
    
    <!-- After -->
    <java.version>17</java.version>
    ```

2.  **Update the Spring Boot Parent:** This single change will update Spring Boot, Spring Framework, and manage many transitive dependencies like Mockito and JUnit 5.
    ```xml
    <!-- Before -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
        <relativePath />
    </parent>
    
    <!-- After -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath />
    </parent>
    ```

3.  **Remove PowerMock Dependencies:** These are no longer needed. Delete these blocks entirely.
    ```xml
    <!-- REMOVE THESE DEPENDENCIES -->
    <dependency>
        <groupId>org.powermock</groupId>
        <artifactId>powermock-module-junit4</artifactId>
        <version>2.0.0-beta.5</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.powermock</groupId>
        <artifactId>powermock-api-mockito2</artifactId>
        <version>2.0.0-beta.5</version>
        <scope>test</scope>
    </dependency>
    ```

4.  **Exclude JUnit 4 from `spring-boot-starter-test`:** The starter now includes JUnit 5 by default, but let's be explicit to avoid conflicts.
    ```xml
     <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    ```

---

### **Phase 2: Refactor Production Code (Eliminate Static Calls)**

The primary reason for using PowerMock is to test code that calls static methods directly. We will refactor this into a standard, injectable Spring service.

1.  **Refactor `NotificationUtil.java`:**
    -   **Action:** Convert the class from a utility with static methods to a standard Spring service.
    -   **File:** `src/main/java/com/javatechie/pm/api/util/NotificationUtil.java`
    
    ```java
    // Before
    public class NotificationUtil {
        public static void sendNotification(String message) {
            // some implementation
            System.out.println("Notification sent: " + message);
        }
    }

    // After
    import org.springframework.stereotype.Service;

    @Service
    public class NotificationUtil {
        public void sendNotification(String message) {
            // some implementation
            System.out.println("Notification sent: " + message);
        }
    }
    ```

2.  **Refactor `OrderService.java`:**
    -   **Action:** Use constructor injection to get an instance of `NotificationUtil` and call its instance method.
    -   **File:** `src/main/java/com/javatechie/pm/api/service/OrderService.java`

    ```java
    // Before (simplified example)
    import com.javatechie.pm.api.util.NotificationUtil;
    
    @Service
    public class OrderService {
        public String placeOrder(OrderRequest request) {
            // ... business logic ...
            NotificationUtil.sendNotification("Order placed successfully");
            return "Order placed";
        }
    }

    // After
    import com.javatechie.pm.api.util.NotificationUtil;
    
    @Service
    public class OrderService {
    
        private final NotificationUtil notificationUtil;

        // Use constructor injection
        public OrderService(NotificationUtil notificationUtil) {
            this.notificationUtil = notificationUtil;
        }

        public String placeOrder(OrderRequest request) {
            // ... business logic ...
            this.notificationUtil.sendNotification("Order placed successfully");
            return "Order placed";
        }
    }
    ```

---

### **Phase 3: Migrate Tests to JUnit 5 and Mockito**

Now, we will update the test class to use the new framework.

1.  **Update `PowermockApiApplicationTests.java`:**
    -   **File:** `src/test/java/com/javatechie/pm/api/PowermockApiApplicationTests.java`
    -   **Actions:**
        1.  Remove all PowerMock and JUnit 4 imports and annotations (`@RunWith`, `@PrepareForTest`).
        2.  Add `@SpringBootTest` to enable Spring Boot features in the test.
        3.  Use `@MockBean` to create a mock of our new `NotificationUtil` service.
        4.  Rewrite the test logic using standard Mockito and JUnit 5 assertions.

    ```java
    // Before (Conceptual)
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.powermock.api.mockito.PowerMockito;
    import org.powermock.core.classloader.annotations.PrepareForTest;
    import org.powermock.modules.junit4.PowerMockRunner;
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest(fullyQualifiedNames = "com.javatechie.pm.api.util.NotificationUtil")
    public class PowermockApiApplicationTests {

        @Test
        public void testOrderService() {
            // Setup static mock
            PowerMockito.mockStatic(NotificationUtil.class);
            PowerMockito.doNothing().when(NotificationUtil.class, "sendNotification", Mockito.anyString());

            // Test logic
            OrderService service = new OrderService();
            service.placeOrder(new OrderRequest());

            // No real verification possible here besides no exception
        }
    }

    // After
    import org.junit.jupiter.api.Test;
    import org.mockito.Mockito;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.boot.test.mock.mockito.MockBean;

    import static org.mockito.Mockito.times;
    import static org.mockito.Mockito.verify;

    @SpringBootTest
    class PowermockApiApplicationTests {

        @Autowired
        private OrderService orderService;

        @MockBean
        private NotificationUtil notificationUtil;

        @Test
        void testPlaceOrder_shouldSendNotification() {
            // Given an order request
            OrderRequest orderRequest = new OrderRequest(/*...details...*/);
            
            // We don't need to mock the behavior of a void method if we're just verifying it was called.
            // Mockito will do nothing by default for void methods.

            // When
            orderService.placeOrder(orderRequest);

            // Then
            // Verify that the sendNotification method was called exactly once with the correct message.
            verify(notificationUtil, times(1)).sendNotification("Order placed successfully");
        }
    }
    ```

---

### **Phase 4: Final Build and Validation**

1.  **Address Namespace Changes (`javax` to `jakarta`):**
    -   Run `mvn clean install`. The build will likely fail due to invalid imports.
    -   Search across the entire project for `import javax.` and replace with `import jakarta.`. This is a critical step for Spring Boot 3.

2.  **Build and Test:**
    -   Run `mvn clean install` again. The build and tests should now pass.

3.  **Manual Verification (QA):**
    -   Start the application: `mvn spring-boot:run`.
    -   Use a tool like `curl` or Postman to send a request to your API endpoints.
    -   Verify that the application logs "Notification sent:..." and returns a successful response.

## 6. Conclusion

By following these steps, the project will be successfully migrated to a modern, secure, and maintainable technology stack. This plan addresses the major challenges systematically, ensuring a high-quality outcome.
