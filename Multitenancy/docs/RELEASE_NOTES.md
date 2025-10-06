# Release Notes

## Highlights
- Netflix-scale readiness uplift (~9.5)
- Supply chain enforcement (signatures, provenance, SBOM)
- DR scaffolding (Istio failover, Postgres/MinIO replication)
- Auth hardening (JWKS rotation, TOTP, WebAuthn, device trust, reuse detection)

## Changes
- Security/Auth: JWKS cache/rotation, logout; refresh reuse detection; session revoke; TOTP; WebAuthn; device trust; audit events
- Operability: SLO burn alerts; chaos steady-state; backup-restore CI drill; optional k6 soak job
- Supply chain: Kyverno signature verification and SLSA provenance + SBOM attestations
- DR: Istio locality failover; Postgres primary/replica; MinIO replication script
- Mesh/Identity: Istio STRICT mTLS; SPIFFE IDs
- CI/CD/GitOps: Pact gate; Helm lint/template; OPA tests; SBOM + Trivy; ArgoCD to remote repo
- Docs: Updated readiness and analysis; reviewer handoff added

## Upgrade Notes
- Apply Kyverno policies and configure Vault/OIDC before rollout
- Ensure Schema Registry subjects set to BACKWARD and schemas registered
- Provide `db-credentials` secret for Postgres manifests

## Rollback
- Disable Kyverno policies if blocking
- Use canary auto-rollback or `kubectl rollout undo`
- Restore DB/objects using provided scripts
