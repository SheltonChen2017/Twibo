# Twibo

Twibo is a small, focused social blogging application originally built as a
student project. It supports accounts, sign-in, private-phrase password recovery,
posts, comments, user search, profiles, following, and a paginated personal feed.

The current version has a responsive, dependency-free interface and deliberately
minimizes registration data: no email address or birthday is required.

## Requirements

- Java 21

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Open <http://localhost:8321>. The default database is persisted under `data/` and
requires no separate database server.

Run tests with:

```powershell
.\mvnw.cmd test
```

## Run with containers

To rehearse the production setup with MySQL:

```powershell
docker compose up --build
```

Open <http://localhost:8080>.

## Optional MySQL configuration

Set these environment variables before starting:

```powershell
$env:DATABASE_URL = "jdbc:mysql://localhost:3306/twibo"
$env:DATABASE_USERNAME = "twibo"
$env:DATABASE_PASSWORD = "replace-me"
```

Use a dedicated database account rather than the MySQL root account.

## Deploy

The repository includes a non-root multi-stage container image, production
configuration, Flyway migrations, health endpoints, and CI. See
[DEPLOYMENT.md](DEPLOYMENT.md) for Azure Container Apps and AWS App Runner
instructions.

## Security note

Passwords and recovery phrases are stored as BCrypt hashes. Authentication and
recovery use generic failure messages, constant-work hash checks, throttling, CSRF
protection, session-ID rotation, short-lived recovery authorization, and a strict
browser content policy. The local H2 database, environment files, and build output
are ignored by Git.

The built-in throttling is process-local and is suitable for a small single-instance
deployment. A multi-replica public deployment should move rate-limit state to a
shared edge, gateway, or store.
