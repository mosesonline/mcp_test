# CLAUDE.md

## Project overview

Spring Boot 4 MCP server exposing AI tools over HTTP with OAuth2 security (Keycloak). The MCP endpoint is `/mcp` and uses the Streamable/synchronous protocol from Spring AI 2.

## Build & run

```bash
./mvnw spring-boot:run                         # requires Docker for compose services
./mvnw test                                    # context load test only (needs running infra)
```

To start with Docker Compose services auto-started (OpenSearch + Keycloak):

```bash
# Run the test main as a dev launcher (activates "local" profile which enables docker-compose)
./mvnw spring-boot:test-run
```

Or run `McpTestApplicationTests.main()` from the IDE — it activates the `local` profile which enables `spring.docker.compose`.

## Key environment variables

Defined in `.envrc` (loaded by `direnv`):

| Variable | Value | Purpose |
|---|---|---|
| `MCP_CLIENT_ID` | `mcp-client` | Keycloak OAuth2 client ID |
| `MCP_CLIENT_KEY` | `g6aLlQUTVJfSsPsji9YsrATRNnDQnv0x` | Keycloak client secret |

## Infrastructure (docker-compose.yaml)

| Service | Port | Notes |
|---|---|---|
| opensearch-node1 | 9200, 9600 | Security disabled for local dev |
| opensearch-node2 | — | Cluster peer |
| opensearch-dashboards | 5601 | UI |
| keycloak | 18080 | Realm: `mcp_realm`, imports from `./keycloak/` |
| database (postgres) | 25432 | Backing store for Keycloak |

## Configuration (application.properties)

- MCP server name: `moses-mcp-test`, protocol: `streamable`, type: `sync`
- Virtual threads enabled
- Local/test profile (`application-local.yaml`) adds: OpenSearch URI, Keycloak JWT issuer URI, Docker Compose config

## Source layout

```
src/main/java/.../mcptest/
  McpTestApplication.java          — entry point
  MosesTools.java                  — MCP tools (@McpTool)
  book_store/
    BookStoreService.java          — OpenSearch multi-match query
    BookStoreConfiguration.java    — seeds "books" index on startup
    model/Book.java                — record: title, author, isbn, description
  configuration/
    SecurityConfiguration.java     — JWT resource server, roles from realm_access.roles
    OAuthProtectedResourceFilter   — serves /.well-known/oauth-protected-resource for MCP OAuth discovery
```

## MCP tools

| Tool | Method | Description |
|---|---|---|
| `moses-tips` | `MosesTools.getTip()` | Returns a hardcoded tip string |
| `moses-search-books` | `MosesTools.searchBooks(searchText)` | Multi-match search on OpenSearch `books` index |

## Security notes

- All requests to `/mcp` require a valid JWT (`Bearer` header)
- JWT issuer: `http://localhost:18080/realms/mcp_realm`
- JWT audience: `http://localhost:8080`
- `OAuthProtectedResourceFilter` exposes `/.well-known/oauth-protected-resource` so MCP clients can discover the auth server automatically — this is required for the Claude Code `.mcp.json` OAuth flow to work
- Swagger UI (`/swagger-ui/**`) and Actuator (`/actuator/**`) are public

## Keycloak

Admin console: `http://localhost:18080` — admin / admin

### Test user
- Username: `mcp_test@mosesnoline.de`
- Password: `test_123!`

### Backup realm config
```bash
docker exec -it mcp-test-keycloak-1 /opt/keycloak/bin/kc.sh export --file=/tmp/mcp_realm.json --users same_file --realm mcp_realm
docker cp mcp-test-keycloak-1:/tmp/mcp_realm.json ./keycloak
```