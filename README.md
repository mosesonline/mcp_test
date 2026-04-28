# MCP Test

## Description

This repo demonstrates a **Model Context Protocol (MCP) server** built with Spring Boot 4 and Spring AI 2, secured via Keycloak OAuth2. It exposes MCP tools over HTTP (Streamable MCP transport) that an AI agent (e.g. Claude Code) can call to interact with a book store backed by OpenSearch.

### Tech stack

| Component | Version  | Role |
|---|----------|---|
| Spring Boot | 4.0.6    | Application framework |
| Spring AI | 2.0.0-M5 | MCP server support (`spring-ai-starter-mcp-server-webmvc`) |
| Keycloak | 26.6     | OAuth2 authorization server (JWT issuer) |
| OpenSearch | latest   | Full-text search index for the book store |
| Java | 26       | Runtime (virtual threads enabled) |

### Architecture

```
Claude Code / AI Agent
        │
        │  HTTP  (OAuth2 Bearer JWT)
        ▼
Spring Boot MCP Server  (http://localhost:8080/mcp)
        │
        ├── Tool: moses-tips          → returns a hardcoded tip
        └── Tool: moses-search-books  → multi-match query on OpenSearch
                                            index: "books"
                                            fields: title, author, isbn, description
```

The server is a **synchronous Streamable MCP server** (`spring.ai.mcp.server.protocol=streamable`, `type=sync`). All endpoints except Swagger UI and Actuator require a valid JWT issued by the configured Keycloak realm.

### MCP tools

| Tool name | Parameters | Description |
|---|---|---|
| `moses-tips` | — | Returns one of Moses' tips |
| `moses-search-books` | `searchText: string` | Full-text search across title, author, ISBN, description |

### OAuth2 / Security

- Resource server validates JWTs from Keycloak realm `mcp_realm`
- Roles are extracted from the `realm_access.roles` claim with prefix `ROLE_`
- The MCP client authenticates via Authorization Code flow with scope `openid mcp:tools`
- Client ID: `mcp-client`, Keycloak runs on port `18080`

### Claude Code integration (`.mcp.json`)

```json
{
  "mcpServers": {
    "moses-mcp-server": {
      "type": "http",
      "url": "http://localhost:8080/mcp",
      "oauth": {
        "clientId": "mcp-client",
        "authorizationUrl": "http://localhost:18080/realms/mcp_realm/protocol/openid-connect/auth",
        "tokenUrl": "http://localhost:18080/realms/mcp_realm/protocol/openid-connect/token",
        "scope": "openid mcp:tools"
      }
    }
  }
}
```

### Seed data

On startup, `BookStoreConfiguration` checks whether the `books` index in OpenSearch is empty and seeds it with one book:

```
Title:  The Lord of the Rings
Author: J.R.R. Tolkien
ISBN:   978-0395647387
```

## Start

```shell
# start database, Keycloak, OpenSearch with docker-compose
docker-compose up -d
# starts application with local profile (loads Keycloak and OpenSearch URIs from application-local.yaml), 
# it would also start the docker containers but it can happen, that they are not ready when the application is up and then it would fail to connect and stop.
./mvnw spring-boot:test-run
http://localhost:18080/realms/mcp_realm/protocol/openid-connect/auth?response_type=code&client_id=mcp-client&code_challenge=g99HhHGL8qOPK0s5mOTO5zA6oOQt8OrEH2vokHUP9CI&code_challenge_method=S256&redirect_uri=http%3A%2F%2Flocalhost%3A63406%2Fcallback&state=QvV8K5Z7jjTnFW6ko-mBq_TY_QK-K1oP5-4LTW7Ktdg&scope=openid+mcp%3Atools+offline_access&resource=http%3A%2F%2Flocalhost%3A8080%2F
```

### Connect with mcp

1. Ensure docker services and service is running
2. Open your AI agent (e.g. Claude Code)
3. Configure the MCP server connection (`/mcp` should list the "moses-mcp-server" server)
4. Select server
5. Select (Re-)Connect
6. Select server, again (server state should be "needs authentication")
7. Select Authenticate (this should open the Keycloak login page in your browser, or copy the provided link if it doesn't open automatically, but make sure you delete spaces or other shell characters if you copy it manually)
8. Login with the user (see Keycloak section below)

## Sample prompts

`search for books by J.R.R Tolkien`

## Keycloak

## User

There is no such email address and I don't use this pw anywhere. So, feel free to use it for testing, but I would change it.

- Username: mcp_test@mosesnoline.de
- Password: test_123!

### Backup keycloak database

```bash
docker exec -it mcp-test-keycloak-1 /opt/keycloak/bin/kc.sh export --file=/tmp/mcp_realm.json --users same_file --realm mcp_realm
docker cp mcp-test-keycloak-1:/tmp/mcp_realm.json ./keycloak
``` 
