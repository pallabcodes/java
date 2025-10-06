# API Versioning and AsyncAPI

## API versioning policy
- URI versioning for public APIs: /api/v1
- Internal APIs may use header based version key X-Api-Version
- Deprecation window and removal policy documented per release notes

## Event schema governance
- AsyncAPI catalog stored in docs/asyncapi directory
- Backward compatible event evolution by adding optional fields
- Use schema registry for Kafka topics with compatibility set to backward

## Next steps
- Create docs/asyncapi catalog with first audit event family
- Add schema registry configuration and lint in CI
