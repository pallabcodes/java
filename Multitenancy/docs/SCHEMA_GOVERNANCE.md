# Schema Governance

## Registry and compatibility
- Use a schema registry for Kafka topics (backward compatibility)
- Validate schemas in CI and enforce compatibility checks

## AsyncAPI catalogs
- Keep AsyncAPI specs in docs/asyncapi
- Version event families and include examples

## Retention and PII
- Define topic retention and compaction policies per event type
- Redact or tokenize PII fields at source
