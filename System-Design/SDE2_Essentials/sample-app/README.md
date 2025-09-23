## SDE2 Essentials Sample App

### Run
```
mvn -q -DskipTests spring-boot:run -f pom.xml
```
or with Makefile:
```
make run
```

### REST with OAuth2
Get a dev token from the app and call the secured endpoint:

```
TOKEN=$(curl -s "http://localhost:8080/dev/token?scope=accounts:read")
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/accounts/a1
```

### gRPC
Server listens on 9090. Use grpcurl:
```
grpcurl -plaintext -d '{"id":"a1"}' localhost:9090 accounts.v1.AccountService/GetAccount
```

### Circuit breaker demo
```
curl http://localhost:8080/payments/ping
```

### Logs
Structured JSON to stdout with trace fields and service tag.

### Tracing with OpenTelemetry
### Postman
Import `postman_collection.json` and run requests.
Start local stack:
```
docker compose up -d
```

Run the app and hit a few endpoints, then check collector logs. Grafana at http://localhost:3000 can be configured to query Tempo at http://tempo:3200.


