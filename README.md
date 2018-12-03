# Intro

moneytransfer is a simple but robust money transfer application, featuring safe transactions and idempotency for critical transfer operations.

```sbt run```

Then go to ```http://localhost:8090/swagger``` to play with REST.

To stop server and exit, press Enter.


# Features

* auto-generated documentation (swagger)
* auto-generated REST client interface (swagger)
* unit tests
* integration tests
* load testing (Gatling)
* transaction-safe money transfer
* duplicate transaction prevention using idempotency key
* resource names according to best practices
* featuring scalastyle for high code quality standards

# Testing

For unit tests, run:

```sbt test```

For integration tests, run:

```sbt it:test```

For load test, run service:

```sbt run```

and then run gatling in separate console.

```sbt gatling:test```

Generated reports are located in target/gatling/ folder

# Playing wih rest

Go to http://localhost:8090/swagger for REST interactive testing and documentation.

http://localhost:8090/swagger-editor allows you to edit description interactievly, just import URL http://localhost:8090/api-docs/swagger-manual.json.

# Config parameters
```
server{

  # server interface
  host = 0.0.0.0

  # server port
  port = 8090

}

idempotency{

  # Time-to-live for idempotency key, in seconds
  TTL = 3600

  # operations between old keys cleanup
  gc = 10000

}
```
