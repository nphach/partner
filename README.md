# Partner

Loan repayment communication platform.

Design docs: [`docs/domain-model.mmd`](docs/domain-model.mmd).

## Docker (build and run)

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running (`docker info` shows a healthy **Server** section).

From the root:

```bash
./build-up.sh
```

That runs `docker compose up --build`: builds the **app** and **server** images, starts **db**, waits for Postgres to be healthy, then starts the API and UI. Logs stay attached to your terminal; stop with `Ctrl+C`.

**Stop and remove containers** (keeps the database volume):

```bash
docker compose down
```

| Service                    | URL                                                      |
| -------------------------- | -------------------------------------------------------- |
| App                        | http://localhost:3000                                    |
| Server API                 | http://localhost:8081/api/health                         |
| PostgreSQL (from your Mac) | `localhost:5433` — user / password / database: `partner` |

Host ports **8081** and **5433** are used so the stack can run alongside other services on 8080 and 5432. Inside Compose, the server still uses port 8080 and Postgres 5432.

**Quick check:**

```bash
curl http://localhost:8081/api/health
curl http://localhost:3000/api/health
```

Both should return `{"status":"ok"}`. The app page at http://localhost:3000 shows the same health check in the UI.

Equivalent without the script: `docker compose up --build` (add `-d` to detach). Extra Compose flags can be passed through, e.g. `./build-up.sh -d --force-recreate`.

## Project layout

```text
partner/
├── app/              # React + Vite + TypeScript
├── server/           # Spring Boot (REST, JPA, PostgreSQL)
├── docs/             # Domain model and design artifacts
├── build-up.sh       # Bring up Compose stack
└── docker-compose.yml
```
