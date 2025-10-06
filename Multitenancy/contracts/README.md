# Event Contracts

- Avro schemas live in src/main/avro and are versioned.
- Schema Registry subject compatibility should be set to BACKWARD.
- Topics:
  - audit.events.v1 -> audit_event.avsc
  - webhook.events.v1 -> webhook_event.avsc

## Set compatibility

Use schema-registry API to set subject compatibility to BACKWARD for each subject name.

## Pact Contracts

Provider publishes verification results to Pact Broker via docker-compose.pact-broker.yml.
