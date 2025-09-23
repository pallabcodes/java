run:
	mvn -q -DskipTests spring-boot:run -f pom.xml

trace-up:
	docker compose up -d

trace-down:
	docker compose down -v

token:
	@curl -s "http://localhost:8080/dev/token?scope=accounts:read" | tee /dev/stderr

account:
	@TOKEN=$$(curl -s "http://localhost:8080/dev/token?scope=accounts:read"); \
	curl -s -H "Authorization: Bearer $$TOKEN" http://localhost:8080/accounts/a1 | tee /dev/stderr

grpc:
	grpcurl -plaintext -d '{"id":"a1"}' localhost:9090 accounts.v1.AccountService/GetAccount | tee /dev/stderr


