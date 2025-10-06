# DR drill runbook

- Objective: verify backup restore, config bootstrap, service health, and event pipeline after failover.
- Scope: reporting-service and core dependencies.

## Steps

1. Prep
   - Verify backups exist and last snapshot timestamp.
   - Freeze prod changes or run in staging.
2. Simulate outage
   - Disable primary DB user or block network to primary DB.
   - Scale reporting-service to zero in primary zone.
3. Restore and failover
   - Restore DB snapshot to standby.
   - Point app to standby via Config Server and refresh.
4. Validate
   - Run smoke tests and synthetic transactions.
   - Verify Kafka consumers recover and process backlog.
   - Check SLO dashboards and alarms.
5. Evidence
   - Export logs, metrics graphs, timestamps, and outcomes.

## Rollback

- Re-enable primary, switch traffic back, scale replicas, and confirm health.

## Contacts

- Oncall primary, database admin, platform SRE.
