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

## Optional MySQL configuration

Set these environment variables before starting:

```powershell
$env:DATABASE_URL = "jdbc:mysql://localhost:3306/twibo"
$env:DATABASE_USERNAME = "twibo"
$env:DATABASE_PASSWORD = "replace-me"
```

Hibernate creates or updates the tables. Use a dedicated database account rather
than the MySQL root account.

## Security note

Passwords and recovery answers are stored as BCrypt hashes. The local H2 database,
environment files, and build output are ignored by Git.
