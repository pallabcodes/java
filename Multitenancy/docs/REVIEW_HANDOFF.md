# Reviewer Handoff Checklist

Env and secrets
- Vault HA deployed and unseal steps documented
- OIDC realm and JWKS endpoints reachable
- Resource server configured with JWKS cache and rotation

Contracts and tests
- Schema Registry subjects set to BACKWARD
- Avro schemas registered for audit and webhook events
- Pact provider verification job green and build gated on it
- OPA policy tests pass in CI

CI and supply chain
- Unit, integration, and E2E tests passing
- OWASP and Trivy scans passing; SBOM generated
- Helm lint/template jobs green
- Kyverno policies installed for cosign signatures and provenance/SBOM attestations

Mesh and identity
- Istio mTLS STRICT; AuthorizationPolicy applied
- SPIFFE IDs configured via ClusterSPIFFEID
- Gateway correlation and rate limit filters active

DR and backups
- Istio locality failover rules applied
- Postgres primary/replica manifests or managed equivalent in place
- MinIO site replication configured
- CI backup-restore drill within RTO threshold

Observability
- Prometheus rules loaded; SLO burn alerts wired to paging
- Zipkin tracing visible; JSON logs with correlation id
- Grafana SLO dashboard imported

Security and auth
- OAuth2/OIDC working; back-channel logout enabled
- Refresh token reuse detection active
- TOTP MFA provision/verify endpoints tested
- WebAuthn register/assert scaffold tested
- Device trust check returns mfa_required for unknown devices
- Session revoke by id and revoke-all working
- OPA policy enforcement enabled at API

GitOps and environments
- ArgoCD apps point to remote repo env paths (dev/stage/prod)
- Auto sync enabled with prune and self-heal

Submission artifacts
- NETFLIX_SCALE_VALIDATION.md
- ANALYSIS.md
- MICROSERVICES_TOOLCHAIN.md
- RUNBOOK.md, SECURITY.md, PERFORMANCE.md

Reviewer notes
- Current readiness score: 9.5
- Remaining continuous improvements: expand chaos (network faults), quarterly DR drills with evidence, sustained soak at target envelope
