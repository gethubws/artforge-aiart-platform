# Setup

## Prerequisites

- JDK 17
- Maven 3.8+
- Node.js 20.19+ or 22+
- pnpm
- MySQL 8
- Redis 7
- Forge or another Stable Diffusion WebUI-compatible server running with API enabled at `http://127.0.0.1:7860`

## Database

Run `database/schema.sql` in MySQL. The default database name is `aiart_codex_platform`.

```bash
mysql -u root -p < database/schema.sql
```

## Backend

From `backend/`:

```bash
.\mvnw.cmd spring-boot:run
```

Useful environment variables:

```bash
AIART_DB_USER=root
AIART_DB_PASSWORD=your_password
AIART_SD_BASE_URL=http://127.0.0.1:7860
AIART_JWT_SECRET=replace_with_a_long_random_secret_at_least_32_bytes
```

The backend serves API routes under `http://127.0.0.1:8080/api` and generated images under `http://127.0.0.1:8080/uploads`.

## Frontend

From `frontend/`:

```bash
pnpm install
pnpm dev
```

Open `http://127.0.0.1:5173`.

## Current Scope

The current baseline implements the first working creation loop: auth, tag-driven prompt assembly, provider discovery, txt2img generation, generated-image storage, and personal library reads. Style packages, points, tasks, and audit tables are present in the schema for the next implementation phase.
