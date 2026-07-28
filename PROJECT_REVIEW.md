# Project review

## Executive summary

The original application was a 2018 Spring Boot 2.0 / Java 8 prototype. Its intent
was clear and ambitious, but it could not run on this machine, required a separately
configured MySQL server with hard-coded root credentials, and had several incomplete
or unsafe user flows. The project has been rebuilt as a focused Spring Boot 3.5 /
Java 21 application that runs locally without infrastructure.

## Major issues found

- No JDK was installed, and the Maven wrapper targeted Maven 3.5.3.
- The default configuration exposed a MySQL root password and assumed a specific
  local database and schema.
- Passwords and recovery answers were compared and stored as plaintext.
- Login loaded every user and compared entity objects instead of querying one user.
- Many repository methods returned lists and immediately accessed index zero, causing
  exceptions for missing records.
- Registration always returned `false`, while several declared methods were stubs.
- Password recovery could not work because loading security questions returned null.
- Mutating operations such as follow, comment, password reset, and logout used GET.
- There was no CSRF protection or session-fixation protection.
- JPA relationships used broad `CascadeType.ALL` and eager fetching in both
  directions, risking accidental writes, huge queries, and recursion.
- Native SQL depended on MySQL-only functions and historical table naming.
- Controllers mixed persistence, authentication, JSON serialization, session state,
  output-stream handling, and view rendering.
- Routes, redirects, and Thymeleaf view names were inconsistent.
- The feed pagination state had off-by-one errors and depended on session attributes
  that might not exist.
- Remote, obsolete Bootstrap/jQuery assets made the UI dependent on third parties.
- The only test merely attempted to load the Spring context against the hard-coded
  database.
- An unrelated 1.8 MB binary `.ini` file was committed inside the test source tree.

## Repairs completed

- Installed Temurin Java 21 and updated the Maven wrapper to Maven 3.9.11.
- Upgraded the application to Spring Boot 3.5.3 and Jakarta Persistence.
- Replaced the legacy model with four bounded entities: user, post, comment, follow.
- Replaced the default database with persistent embedded H2; MySQL remains optional
  through environment variables.
- Removed all hard-coded credentials.
- Added BCrypt hashing for passwords and recovery answers.
- Added validation, case-insensitive account lookup, safe not-found handling, and
  transactional service boundaries.
- Added session authentication, session-ID rotation after login, protected routes,
  POST-only mutations, SameSite/HttpOnly cookies, and CSRF protection.
- Implemented registration, login/logout, recovery/reset, publishing, commenting,
  search, profiles, follow/unfollow, and personalized feeds.
- Removed dangerous eager/cascading graphs and explicitly fetch only data each view
  needs.
- Replaced obsolete templates and remote frontend dependencies with responsive local
  HTML/CSS while retaining the original image assets.
- Added integration tests covering startup, route protection, CSRF, account creation,
  publishing, feed rendering, commenting, and conversation rendering.
- Rewrote the README with current run and database instructions.

## Verification

- `mvnw package`: successful
- Automated tests: 4 passed, 0 failed
- Executable JAR created: `target/twibo-1.0.0-SNAPSHOT.jar`
- Runtime smoke test with the persistent H2 database: successful
- `GET /`: HTTP 200
- `GET /login`: HTTP 200 and CSRF token present
- `git diff --check`: no whitespace errors

## Remaining production considerations

This is now a functional local application, not a production-scale social network.
Before public deployment:

- Replace security-question recovery with time-limited email recovery tokens.
- Add rate limiting for login, recovery, posting, and search.
- Add feed pagination and database indexes based on measured query plans.
- Use Flyway or Liquibase migrations instead of Hibernate schema updates.
- Configure HTTPS, secure cookies, proxy headers, centralized logs, monitoring,
  backups, and a managed database.
- Add moderation, account deletion, privacy controls, accessibility testing, and
  broader browser/end-to-end tests.
- Decide whether reposts and notifications should return; the original implementations
  were internally inconsistent and were omitted rather than preserving broken data
  behavior.
