# Istio Validation Steps

## Validate mTLS
- Apply PeerAuthentication to enforce STRICT
- Confirm traffic uses mTLS via telemetry

## Traffic splitting
- Create DestinationRule with subsets v1 and v2
- Create VirtualService with 90/10 split

## Authorization policy
- Allow only gateway service account to call api service

## Canary checklist
- Health, error rate, latency within budget before promote
