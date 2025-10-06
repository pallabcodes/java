# Vault Integration

## Goals
- Store secrets outside app config
- Dynamic database credentials

## Plan
- Enable Spring Cloud Vault with token auth in dev
- Map secrets to spring config keys
- Rotate DB creds on schedule with runbook

## Properties example
```
spring:
  cloud:
    vault:
      uri: http://localhost:8200
      token: dev-root-token
      kv:
        enabled: true
        backend: secret
```
