# Config and Progressive Delivery

## Externalized configuration
- Use Spring Cloud Config for all environment specific settings
- Enable encryption for sensitive values
- Add drift detection with commit hash comparison in CI

## Progressive delivery
- Canary or blue/green via Istio and GitHub Actions
- Automated rollback on SLO breach using alert webhooks
- Feature flags via Unleash or LaunchDarkly as optional
