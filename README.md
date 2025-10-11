# e-commerce-app

A backend system for an e-commerce platform

---

### ⚙️ Tech Stack

- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Cloud Gateway** for API routing
- **Resilience4j** for fault tolerance (circuit breakers, retries)
- **Spring Security** for basic authentication and route protection
- **Eureka** for service discovery
- **JWT** for stateless authentication
- **Docker**

---

### 🔐 Security

- **Spring Security** is configured for route-level protection.
- Authentication uses **JWT tokens** for stateless auth.
- Sensitive endpoints are protected and require a valid token for access.

---

### 🛡️ Resilience Strategy

- Internal service-to-service calls use **Resilience4j** annotations like `@Retry`, `@CircuitBreaker`, and `@RateLimiter` to handle downstream instability.
- External client requests go through **Spring Cloud Gateway**, where resilience is enforced via filters such as:
  - `Retry`
  - `CircuitBreaker` (with fallback support)
- **Gateway filters** are configured using `application.yml` with custom circuit breaker instances.
- **Internal service calls bypass the Gateway**, preventing duplicate retries and ensuring clean separation of internal vs. external resilience handling.

---

### 🐳 Docker Compose

To streamline starting and stopping all microservices and related components at once, a **Docker Compose** setup is included. It allows you to:

- Start all applications and dependencies (e.g., Eureka server, Gateway, services) with a single command.
- Shut down all running containers cleanly.

**Usage:**

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down
```
---