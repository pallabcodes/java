### Security E2E test plan

**Objectives**

1. Validate authentication, session, and refresh flows
2. Enforce authorization and tenant isolation across endpoints
3. Verify rate limiting, throttling, and anomaly detection
4. Confirm audit logs and webhooks for security events

**Scenarios**

1. Login valid and invalid credentials, throttling after max attempts
2. Access with expired, malformed, wrong-audience tokens
3. Access cross tenant resources should be forbidden
4. Permission changes invalidate cached permissions
5. Back channel logout revokes sessions across nodes
6. Refresh token rotation and reuse detection triggers revocation
7. OPA policy deny returns 403 with stable error code
8. mTLS enabled service call acceptance and rejection without client cert

**Non functional**

1. p95 latency under load with security controls on
2. Log redaction for secrets and tokens
3. Alerts for auth failures and 429 spikes

**Deliverables**

1. JUnit E2E tests with Testcontainers
2. Postman collection flows
3. PromQL alerts and dashboards validation checklist

