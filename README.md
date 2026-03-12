# Project Overview
`e-commerce-app` is a Spring Boot + Spring Cloud microservices backend for core e-commerce workflows: authentication, user management, catalog, cart, and order processing.

It uses an API Gateway entrypoint, centralized configuration, Eureka-based service discovery, synchronous service-to-service calls (OpenFeign), and Kafka-based asynchronous event processing.

# Architecture
The system follows a distributed microservice architecture:

- External traffic enters through `api-gateway`.
- `eureka-server` provides runtime service discovery.
- `config-server` provides centralized configuration loading.
- Domain services (`auth-service`, `user-service`, `product-service`, `cart-service`, `order-service`) are independently deployable.
- Communication is mixed:
  - Synchronous HTTP (OpenFeign)
  - Asynchronous messaging (Kafka)
- Redis is used for caching, auth state, rate-limiting state, and idempotency support.

# Services
| Service | Module Directory | Default Port | Responsibility |
|---|---|---:|---|
| api-gateway | `api-gateway` | 8080 | External entrypoint, JWT validation, RBAC, routing, rate limiting, Swagger aggregation |
| auth-service | `auth-service` | 8081 | Authentication, access/refresh token issuance, refresh token rotation |
| user-service | `user` | 8082 | User registration, profile retrieval, account lifecycle |
| product-service | `product` | 8083 | Product catalog APIs, inventory update on order events |
| cart-service | `cart` | 8084 | Shopping cart operations |
| order-service | `order` | 8085 | Order placement, order queries, order event publishing |
| config-server | `config-server` | 8888 | Centralized externalized configuration |
| eureka-server | `eureka-server` | 8761 | Service registry and discovery |

# System Architecture
- API Gateway routing:
  - `api-gateway` routes `/auth/**`, `/users/**`, `/products/**`, `/carts/**`, `/orders/**`.
  - Per-route retry and Redis-backed rate limiting are enabled.
- Eureka service discovery:
  - Services register themselves and are resolved via `lb://SERVICE-NAME` in gateway/service clients.
- OpenFeign service communication:
  - Used for synchronous inter-service API calls where request/response semantics are required.
- Kafka event-driven messaging:
  - `order-service` publishes order events.
  - `product-service` consumes events to apply inventory changes asynchronously.
- Redis caching and auth state:
  - Refresh tokens and related auth session state.
  - Gateway rate-limiter state.
  - Cache/idempotency keys (including Kafka processing idempotency).

# Event Flow
Order to inventory flow:

1. `order-service` creates an order and publishes `order.placed.v1` (`OrderPlacedEvent`).
2. Event payload includes: `eventId`, `eventType`, `occurredAt`, `orderId`, `userId`, `items[]`.
3. `product-service` consumes the event and updates inventory.
4. Consumer applies schema/validation checks, retry strategy, DLT handling, and idempotent processing with Redis.
5. Correlation ID is propagated through headers and logs for traceability.

# Security
- JWT authentication:
  - Tokens are signed using shared secret from `JWT_SECRET` (`security.jwt.secret`).
  - Gateway validates JWT for protected routes.
- Refresh token rotation:
  - Refresh token is rotated on successful refresh.
  - Token type checks are enforced.
- Redis token storage:
  - Refresh token state is stored/validated in Redis.
  - Prevents stale token reuse and supports invalidation workflows.
- RBAC enforced at gateway:
  - Route-level role checks (`USER`, `ADMIN`) are enforced in gateway security config.

# Observability
- Spring Boot Actuator:
  - Enabled across services.
  - Exposed endpoints include: `health`, `info`, `metrics`, `prometheus`.
- Prometheus metrics:
  - Metrics endpoint exposed for scraping.
- Correlation IDs:
  - `X-Correlation-ID` is propagated across HTTP and Kafka paths.
- Tracing:
  - Trace fields (`traceId`, `spanId`, `correlationId`) are included in structured logs.

# API Documentation
Swagger/OpenAPI is exposed through the API Gateway and aggregates all service APIs.

- Gateway Swagger UI:
  - `http://localhost:8080/swagger-ui.html`
- Service-level docs:
  - `/v3/api-docs`
  - `/swagger-ui.html`

# Project Structure
The repository is organized into domain services plus infrastructure services:

- Domain services: `auth-service`, `user`, `product`, `cart`, `order`
- Infrastructure services: `api-gateway`, `eureka-server`, `config-server`
- Supporting assets: `docs`, `docker-compose.yml`

Each service follows a simple layered package style (`controller`, `service`, `repository`, `dto`, `config`, `exception`), and security-related code is kept in `auth-service` and `api-gateway`.

# Build and Run
Build each service locally:

```bash
cd auth-service && mvn clean install
cd ../user && mvn clean install
cd ../product && mvn clean install
cd ../cart && mvn clean install
cd ../order && mvn clean install
cd ../api-gateway && mvn clean install
cd ../config-server && mvn clean install
cd ../eureka-server && mvn clean install
```

Start all services and dependencies with Docker:

```bash
docker-compose up -d
```

Stop and remove containers:

```bash
docker-compose down
```

# Environment Variables
| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | Shared JWT signing key used by `auth-service` and `api-gateway` |
| `DB_PASSWORD` | Yes | Database password used by service datasources |
| `DB_USERNAME` | Yes | Database username used by service datasources |
| `REDIS_HOST` | No (default: `localhost`) | Redis host for cache/auth/rate-limit state |
| `KAFKA_HOST` | No (default: `localhost`) | Kafka bootstrap host for event messaging |

Use `.env.example` as the template for your local `.env`.

# Tech Stack
- Spring Boot
- Spring Cloud
- Spring Security
- Apache Kafka
- Redis
- Docker / Docker Compose
- Netflix Eureka
- Spring Cloud Gateway
