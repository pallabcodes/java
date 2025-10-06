## Autoscaling tuning runbook

### Inputs
- RED metrics and JVM telemetry
- p95 and p99 latency targets
- CPU and memory utilization ranges

### Steps
1. Capture baseline under soak for 60 minutes.
2. Derive target average CPU per pod and set HPA target.
3. Size minReplicas for steady traffic; cap maxReplicas based on budgets.
4. Tune readiness, liveness, and connection pools.
5. Validate under step load and record saturation points.

### Outputs
- Updated HPA values and pool sizes
- Evidence graphs and summary


