# Alert Rules - Production Notes

## SLO alerts

- Error rate > 0.1% over 5m (burn rate multi window)
- p99 latency > objective for 5m and 1h windows

## Prometheus examples

```promql
# Error rate (burn rate short)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/
sum(rate(http_server_requests_seconds_count[5m])) > 0.001

# Latency SLO breach (p99)
histogram_quantile(0.99, sum by (le)(rate(http_server_request_duration_seconds_bucket[5m])))
  > 0.100
```

## Operational notes

- Pair short window with long window to reduce noise
- Page only on sustained breaches; ticket on early warnings

## Deep Dive Appendix

### Adversarial scenarios
- Alert floods and paging fatigue
- Hidden outages due to missing burn rate alerts
- Flaky alerts from noisy metrics

### Internal architecture notes
- Multi window multi burn SLO alerts; symptom vs cause alerts
- Inhibition and deduplication; silence workflows
- Runbook links and auto remediation hooks

### Validation and references
- Synthetic alert fire drills; historical backtests
- SRE literature on SLOs and alerting best practices

### Trade offs revisited
- Sensitivity vs noise; coverage vs complexity

### Implementation guidance
- Standardize alert templates; tie every alert to a runbook; measure alert quality
