# AiArt Platform

A clean restart of the AI art community platform. This repository is the new working baseline; the older `aiart_openclaw` project is treated as reference material only.

## Baseline

- Backend: Spring Boot 3.2.3, MyBatis-Plus, MySQL, Redis, JWT, WebClient
- Frontend: Vue 3, Vite, Element Plus, Pinia-ready structure, Axios
- Generation provider: Stable Diffusion WebUI-compatible API at `http://127.0.0.1:7860`, including Forge-compatible txt2img options
- Database: `aiart_codex_platform`

## Layout

```text
backend/    Spring Boot API service
frontend/   Vue application
database/   SQL schema and seed data
docs/       product and engineering notes
```

## First Run

1. Create the database with `database/schema.sql`.
2. Configure backend environment variables as needed:
   - `AIART_DB_USER`
   - `AIART_DB_PASSWORD`
   - `AIART_SD_BASE_URL`
   - `AIART_JWT_SECRET`
3. Start the backend from `backend/` with `mvnw.cmd spring-boot:run` on Windows, or global Maven if installed.
4. Start the frontend from `frontend/` with `pnpm install` and `pnpm dev`.

The backend includes a Windows Maven wrapper and has passed `mvnw.cmd -q test` in this workspace.
