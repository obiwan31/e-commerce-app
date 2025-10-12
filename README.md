# e-commerce-app

A backend system for an e-commerce platform.

### 📁 Project Structure

```text
.
├── api-gateway
├── auth-service
├── cart
├── config-server
├── eureka-server
├── order
├── product
├── redis
├── user
└── docker-compose.yml
```

---

### ⚙️ Tech Stack

- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Cloud Gateway** for API routing
- **Eureka** for service discovery
- **Spring Security** for authentication and route protection
- **JWT** for stateless authentication
- **Docker**
- **Redis**
- **Resilience4j** for fault tolerance (circuit breakers, retries)

---

### 🔐 Security

- **Spring Security** is configured for route-level protection.
- Authentication is based on **JWT tokens**, enabling stateless authentication.
- Sensitive endpoints require a valid JWT token for access.

---

### 🧠 Redis

- Used for caching in the Product service.
- Implements login attempt limiting using Redis TTL.
- Maintains a JWT blacklist to handle forced logout or deleted users.

---

### 🛡️ Resilience Strategy

- Internal service-to-service communication uses **Resilience4j** annotations such as `@Retry`, `@CircuitBreaker`, and
  `@RateLimiter` to handle downstream failures.
- External client requests are routed through **Spring Cloud Gateway**, where resilience is managed using:
    - `Retry`
    - `CircuitBreaker` (with fallback support)

- **Gateway filters** are defined in `application.yml` with custom circuit breaker configurations.
- **Internal service calls bypass the Gateway**, avoiding redundant retries and clearly separating internal and external
  resilience handling.

---

### 🐳 Docker Compose

A **Docker Compose** setup is included to simplify the process of starting and stopping all microservices and
dependencies.

**Usage:**

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down
