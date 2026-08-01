# Twibo

Twibo is a small social blogging application originally built as a student project.
It supports accounts, sign-in, password recovery, posts, comments, user search,
profiles, following, and a personalized feed.

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

## Social trading foundation

Twibo now includes the application-side foundation for an eventual Trading
Agent integration:

- Spring Security owns authentication and protects every non-public route.
- Broker connection metadata is private and scoped to its Twibo owner.
- `/settings/connections` provides the future connection-management surface.
- `/trading` is read-only and refuses to call Trading Agent unless the signed-in
  user has an active Alpaca paper connection.
- Twibo never accepts broker credentials, OAuth tokens, approval phrases, or
  orders in this milestone.

When Trading Agent's multi-user API is ready, configure its private base URL
and a service token:

```powershell
$env:TRADING_AGENT_BASE_URL = "http://localhost:8787"
$env:TRADING_AGENT_API_TOKEN = "replace-with-a-long-random-token"
```

The future service contract is documented in `TRADING_AGENT_API.md`. Alpaca
OAuth exchange, token storage, and trading actions are deliberately deferred.
If the API is absent, Twibo's social features remain fully operational.

## Deploy

The repository includes a non-root multi-stage container image, production
configuration, Flyway migrations, health endpoints, and CI. See
[DEPLOYMENT.md](DEPLOYMENT.md) for Azure Container Apps and AWS App Runner
instructions.

## Security note

Passwords and recovery answers are stored as BCrypt hashes. The local H2 database,
environment files, and build output are ignored by Git.
