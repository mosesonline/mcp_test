# MCP Test

## Description

This repo demonstrates a **Model Context Protocol (MCP) server** built with Spring Boot 4 and Spring AI 2, secured via Keycloak OAuth2. It exposes MCP tools over HTTP (Streamable MCP transport) that an AI agent (e.g. Claude Code) can call to interact with a book store backed by OpenSearch.

### Tech stack

| Component | Version | Role |
|---|---|---|
| Spring Boot | 4.0.5 | Application framework |
| Spring AI | 2.0.0-M4 | MCP server support (`spring-ai-starter-mcp-server-webmvc`) |
| Keycloak | 26.6 | OAuth2 authorization server (JWT issuer) |
| OpenSearch | latest | Full-text search index for the book store |
| Java | 26 | Runtime (virtual threads enabled) |

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
# start application with local profile (loads Keycloak and OpenSearch URIs from application-local.yaml)
./mvnw spring-boot:test-run
```

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
