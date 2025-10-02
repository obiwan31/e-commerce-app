# e-commerce-app
A backend system for an e-commerce platform

### 🛡️ Resilience Strategy

- Internal service-to-service calls use fine-tuned **Resilience4j** annotations (`@Retry`, `@CircuitBreaker`, etc.) for handling downstream failures.
- External client requests go through **Spring Cloud Gateway**, which applies resilience using built-in filters (e.g. `Retry`, `CircuitBreaker`, `Fallback`).
- Internal calls **bypass the Gateway**, ensuring clean separation and preventing double retries.