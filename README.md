# Twibo

Twibo is a small social network inspired by Twitter and Weibo. It supports account registration and login, user search, posts, follows, comments, reposts, and password recovery.

The project has been modernized from Spring Boot 2.0/Java 8 to Spring Boot 3.5 and Java 17. It now uses Spring Security, BCrypt password storage, CSRF protection, safe JSON DTOs, database-backed pagination, and an automated integration test suite.

## Requirements

- JDK 17 or newer
- No external database is required for local development

The Maven wrapper downloads the correct Maven version automatically.

## Run locally

On Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\path\to\your\jdk"
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
export JAVA_HOME=/path/to/your/jdk
./mvnw spring-boot:run
```

Open <http://localhost:8321>. By default, Twibo stores data in an H2 database under `./data/`, which is ignored by Git.

## Run the tests

```powershell
.\mvnw.cmd test
```

The tests use an isolated in-memory H2 database.

## Use MySQL

Activate the `mysql` profile and provide credentials through environment variables:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://localhost:3306/springBoot?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8"
$env:DB_USERNAME = "twibo"
$env:DB_PASSWORD = "replace-me"
.\mvnw.cmd spring-boot:run
```

Other supported settings include `PORT`, `DB_DDL_AUTO`, `SESSION_COOKIE_SECURE`, `TWIBO_FEED_PAGE_SIZE`, and `TWIBO_PASSWORD_RESET_TTL_MINUTES`.

For an internet-facing deployment, use a dedicated least-privilege database account, set `SESSION_COOKIE_SECURE=true`, terminate TLS at the application or trusted reverse proxy, and manage schema changes explicitly rather than relying on `DB_DDL_AUTO=update`.

## Legacy database compatibility

The entity mappings retain the original table and key column names. Existing plaintext passwords and security answers can still be verified once; after a successful login or recovery check they are replaced with BCrypt hashes. Back up an existing MySQL database before the first upgraded launch.
