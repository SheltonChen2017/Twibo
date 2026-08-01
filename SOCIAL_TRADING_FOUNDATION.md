# Social trading foundation

This milestone prepares Twibo for a future multi-user Trading Agent connection
without depending on or changing Trading Agent.

## Implemented now

- Spring Security form authentication backed by Twibo's BCrypt password hashes.
- Private-by-default route authorization; only signup, login, recovery, static
  assets, and health probes are public.
- Owner-scoped `broker_connection` metadata for provider, environment, status,
  safe display label, scopes, and timestamps.
- A private `/settings/connections` page.
- A read-only `/trading` page that fails closed until its owner has an active
  Alpaca paper connection.
- A future private API contract using a service bearer token plus the
  authenticated Twibo owner ID.

The broker connection table intentionally has no credential, secret, access
token, refresh token, account number, approval phrase, or order columns.

## Deliberately deferred

- Alpaca OAuth authorization and callback routes.
- OAuth token exchange, encryption, rotation, revocation, and deletion.
- Creation of a `CONNECTED` broker record.
- Trading Agent API implementation.
- Proposal approval or order execution from Twibo.
- Live brokerage connections.
- Public sharing of portfolio or trading data.
- Replacement of the legacy security-question recovery flow with verified
  email recovery (and optional MFA) before a public launch.

Until those parts receive their own reviewed milestone, the connection button
stays disabled and Trading Agent is never contacted for an unconnected user.

## Future trust boundary

The initial private BFF contract uses `TRADING_AGENT_API_TOKEN` to authenticate
Twibo as a service and `X-Twibo-User-Id` to select the already-authenticated
owner. Trading Agent must ignore owner identifiers from query parameters and
scope every database and broker lookup to this authenticated owner.

Before either service becomes internet-addressable, replace that private
contract with short-lived signed assertions containing an immutable subject,
audience, scopes, expiry, and unique token ID.
