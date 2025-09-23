.PHONY: run test itest migrate verify clean

run:
	mvn spring-boot:run

test:
	mvn -q -DskipITs test

itest:
	mvn -q -DskipUTs verify

migrate:
	mvn -q flyway:migrate

verify:
	mvn -q clean verify

clean:
	mvn -q clean
