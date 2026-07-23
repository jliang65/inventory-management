# Inventory Management API

Spring Boot REST API for products, categories, suppliers, locations, inventory, purchase orders, and JWT auth. Uses PostgreSQL and Flyway.

Requires Java 17+ and Docker (for Compose / tests).

## Features

- Products, categories, and suppliers
- Locations and inventory levels (including low-stock)
- Stock in, stock out, adjust, and transfer
- Purchase orders (create, submit, receive, cancel)
- JWT auth with `ADMIN` and `STAFF` roles
- Swagger UI for trying endpoints

## Tech stack

- Java 17, Spring Boot 3.4
- Spring Security (JWT), Spring Data JPA
- PostgreSQL 16, Flyway
- springdoc-openapi (Swagger)
- Testcontainers for integration tests
- Docker Compose



## Run with Docker

1. Create a `.env` file in the project root:

```env
SECURITY_JWT_SECRET=replace-with-a-long-random-secret
```

You can generate one with:

```bash
openssl rand -base64 32
```

1. Start everything:

```bash
docker compose up --build
```

- API: [http://localhost:8080](http://localhost:8080)
- Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Postgres: localhost:5433

To stop and wipe the database volume:

```bash
docker compose down -v
```

`.env` and `application-local.properties` are gitignored — don't commit them.

## Auth

On startup, Flyway seeds a default admin account for **development only** — change or remove it before any real use:

- Email: `admin@example.com`
- Password: `Admin@123!`

Register and login are public. Everything else needs a Bearer token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin@123!"}'

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"password123"}'
```

New registered users get the `STAFF` role. Use Swagger's Authorize button with the `accessToken` from login.

Roles:

- Both `ADMIN` and `STAFF` can read data and do stock in/out/transfer.
- On purchase orders, `STAFF` can create orders, edit draft orders/items, receive submitted orders, and cancel draft orders. Submitting an order requires `ADMIN`.
- Writes to categories, suppliers, products, and locations require `ADMIN`. Same for inventory adjust/create, canceling a submitted purchase order, and changing a user's role.

API details are in Swagger — not listed here.

## Local development

Start only Postgres:

```bash
docker compose up postgres
```

Create `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/inventory_db
spring.datasource.username=inventory_user
spring.datasource.password=inventory_password

security.jwt.secret=replace-with-a-long-random-secret
```

Then run:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```



## Tests

Needs Docker. Testcontainers starts its own PostgreSQL container, so the Compose database does not need to be running.

```bash
./mvnw test
```

