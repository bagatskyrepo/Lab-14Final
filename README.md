## Lab 10 HTTP Server (Spring Boot)

This is a simple Spring Boot application used to practice HTTP concepts: requests, responses,
status codes, headers, and basic persistence with a `users` table.

### Tech stack
- Java 17
- Spring Boot (web, data JPA, validation, security)
- Flyway for database migrations
- SQLite as the database

### Setup
1. Create a `.env` file in the project root (next to `pom.xml`) with:
   - `DB_URL=jdbc:sqlite:database.db`
   - `DB_DRIVER=org.sqlite.JDBC`
2. Make sure `.env` and `*.db` are **not committed** to git (already covered by `.gitignore`).
3. Run the application:

```bash
./mvnw spring-boot:run
```

Flyway will create the `users` table on startup using the migration in `src/main/resources/db/migration`.

### HTTP endpoints
- `GET /api/hello` – simple text response `"Hello, user!"`
- `POST /api/users` – create a user (JSON body: `username`, `email`, `password`)
- `POST /api/login` – simple authentication using email + password (no hashing yet, for demo only)


