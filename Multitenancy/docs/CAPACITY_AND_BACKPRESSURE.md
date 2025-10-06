# Capacity and Backpressure

## Policies
- Set concurrency limits per endpoint and per tenant
- Apply queue length bounds for background workers
- Shed load with 503 when saturation occurs

## Signals
- Queue depth, thread pool utilization, DB connection usage
- Rate limit rejections and webhook retry backlog

## Runbook
- Scale out when sustained p95 > target and queues grow
- Increase rate limits only after confirming downstream capacity
