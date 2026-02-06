Ось повний, професійно оформлений файл `README.md`, який базується на вашому чек-листі. Він написаний англійською мовою (стандарт для GitHub), але структурований так, щоб викладач міг просто копіювати команди та перевіряти пункти один за одним.

Скопіюйте код нижче і вставте його у файл `README.md` у корені вашого проєкту.

```markdown
# Lab 10-14: Secure HTTP API with Spring Boot

This project demonstrates a secure, layered REST API built with Spring Boot. It implements industry-standard security practices including JWT authentication, Role-Based Access Control (RBAC), data isolation, input validation, and secure logging. It also includes a full CI/CD pipeline using GitHub Actions.

## 🛠 Tech Stack
- **Core:** Java 17, Spring Boot (Web, Security, Data JPA, Validation)
- **Database:** SQLite (Development), H2 (Testing)
- **Security:** Spring Security, JJWT (JSON Web Token), BCrypt
- **Migration:** Flyway
- **Testing:** JUnit 5, Mockito, MockMvc, Testcontainers
- **CI/CD:** GitHub Actions, OWASP Dependency Check, JaCoCo

---

## 🚀 Setup & Execution (Application Readiness)

### 1. Prerequisites
Create a `.env` file in the project root (if not already present):
```properties
DB_URL=jdbc:sqlite:database.db
DB_DRIVER=org.sqlite.JDBC

```

### 2. Run the Application

To ensure a clean demo state, remove the old database before starting:

```bash
rm database.db
./mvnw clean spring-boot:run

```

*The application starts on port `8080`. Flyway automatically creates tables.*

### 3. Run Tests

To execute all unit and integration tests:

```bash
./mvnw clean test

```

*JaCoCo coverage report location:* `target/site/jacoco/index.html`

---

## 🧪 Demo Checklist & Verification Commands

Use the following `curl` commands to verify the grading criteria.

### 🔐 1. Authentication & Validation

**✅ Registration: Invalid Input (Expect 400 Bad Request)**
*Demonstrates DTO validation (@NotNull, @Email).*

```bash
curl -v -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "BadUser", "email": "not-email", "password": "123"}'

```

**✅ Registration: Success (Expect 201 Created)**
*Registers user "Ivan". Password is securely hashed using BCrypt.*

```bash
curl -v -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "Ivan", "email": "ivan@test.com", "password": "securePass123"}'

```

**❌ Login: Failure (Expect 401 Unauthorized)**
*Logs generic error message to prevent enumeration.*

```bash
curl -v -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ivan@test.com", "password": "WRONG_PASS"}'

```

**✅ Login: Success (Expect 200 OK + Tokens)**

```bash
curl -v -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ivan@test.com", "password": "securePass123"}'

```

> ⚠️ **IMPORTANT:** Copy the `accessToken` from the response. You will need it as `TOKEN_IVAN`.

---

### 🛡️ 2. Authorization & Data Isolation (Critical)

**✅ User A (Ivan) Creates a Private Note**

```bash
curl -v -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer TOKEN_IVAN" \
  -H "Content-Type: application/json" \
  -d '{"content": "Private data of Ivan"}'

```

**👮 The "Hacker" Scenario (User Isolation Test)**

1. **Register "Hacker" (User B):**

```bash
curl -v -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "Hacker", "email": "hacker@test.com", "password": "hacker123"}'

```

2. **Login "Hacker":**

```bash
curl -v -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "hacker@test.com", "password": "hacker123"}'

```

> ⚠️ Copy the `accessToken` as `TOKEN_HACKER`.

3. **Verify: Hacker CANNOT see Ivan's data (Expect Empty List)**
*FindAll returns only the current user's notes.*

```bash
curl -v -X GET http://localhost:8080/api/notes \
  -H "Authorization: Bearer TOKEN_HACKER"

```

4. **Verify: Hacker CANNOT access Ivan's Note by ID (Expect 403 Forbidden)**
*Access control checks `user_id` ownership.*

```bash
curl -v -X GET http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer TOKEN_HACKER"

```

5. **Verify: Hacker CANNOT Modify Ivan's Data (Expect 403 Forbidden)**

```bash
curl -v -X PUT http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer TOKEN_HACKER" \
  -H "Content-Type: application/json" \
  -d '{"content": "HACKED BY USER B"}'

```

6. **Verify: Hacker CANNOT Delete Ivan's Data (Expect 403 Forbidden)**

```bash
curl -v -X DELETE http://localhost:8080/api/notes/1 \
  -H "Authorization: Bearer TOKEN_HACKER"

```

---

### 👮 3. Role-Based Access Control (Admin)

**1. Register Admin:**

```bash
curl -v -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "Boss", "email": "super_admin@test.com", "password": "adminPass123"}'

```

**2. Login Admin:**

```bash
curl -v -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "super_admin@test.com", "password": "adminPass123"}'

```

> ⚠️ Copy the `accessToken` as `TOKEN_ADMIN`.

**3. Admin Deletes User (Expect 204 No Content)**

```bash
curl -v -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer TOKEN_ADMIN"

```

---

### 🔄 4. Token Management

**Refresh Token Rotation:**
*Use the `refreshToken` UUID from the login response.*

```bash
curl -v -X POST http://localhost:8080/api/refreshtoken \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_UUID"}'

```

---

### ⚙️ 5. Security Headers & Logging

**Check Security Headers:**
*Verifies `X-Content-Type-Options`, `Content-Security-Policy`, etc.*

```bash
curl -v http://localhost:8080/api/hello

```

**Secure Logging:**
Check the server console output.

* ✅ Failed login attempts are logged.
* ✅ Unauthorized access (403) attempts are logged.
* ✅ Passwords and PII are **NOT** logged.

---

## 🤖 CI/CD Pipeline (GitHub Actions)

The repository includes a configured workflow in `.github/workflows/maven.yml` that performs:

1. **Checkout & Build:** Compiles the project.
2. **Automated Testing:** Runs Unit and Integration tests.
3. **OWASP Dependency Check:** Scans for high-severity vulnerabilities in dependencies.
4. **JaCoCo Report:** Generates code coverage artifacts.

```

```
