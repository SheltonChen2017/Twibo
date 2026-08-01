# Trading Agent API contract

Twibo's `/trading` page uses one read-only endpoint:

```http
GET /v1/me/briefing
Authorization: Bearer <TRADING_AGENT_API_TOKEN>
X-Twibo-User-Id: <authenticated Twibo user ID>
Accept: application/json
```

The response is the Trading Agent `DecisionPacket.to_dict()` JSON representation.
Twibo consumes schema version `2.x` and ignores unknown fields, allowing the
packet to grow without coupling releases of the two applications.

The service should also expose an unauthenticated `GET /health` endpoint for
container health probes. It must:

- compare the bearer token using a constant-time operation;
- trust the user header only after authenticating the Twibo service and scope every
  lookup to that owner; a caller-provided query parameter must never select an owner;
- bind to a configurable host and port;
- compute all financial values through Trading Agent's existing deterministic
  `build_decision_packet` function;
- return `Cache-Control: no-store`;
- never include API keys, account identifiers, proposal approval phrases,
  idempotency keys, or order-execution operations;
- keep live trading disabled and leave all approvals/execution in Trading
  Agent's own interface.

Recommended environment contract:

| Setting | Required | Purpose |
|---|---:|---|
| `TRADING_AGENT_API_TOKEN` | Yes | Shared bearer token |
| `PORT` | No | Listener port, default `8787` |
| `APCA_API_KEY_ID` | For Alpaca | Existing Alpaca credential |
| `APCA_API_SECRET_KEY` | For Alpaca | Existing Alpaca credential |

The shared token plus user header is a private BFF contract for this first
milestone, not a public authentication protocol. Replace it with short-lived,
audience-restricted signed assertions before exposing Trading Agent outside a
private network.

For local development, run the agent API on port `8787`, then set Twibo's
`TRADING_AGENT_BASE_URL=http://localhost:8787`. In AWS or Azure, deploy the two
containers independently and connect them over private networking.
