# Roadmap

## Product Direction

Build a real, usable AIGC creation platform rather than a thesis demo. The first product spine is:

1. User accounts and personal workspace.
2. Structured tag selection for prompt assembly.
3. Forge/Stable Diffusion WebUI-compatible image generation.
4. Artwork library with prompt and parameter traceability.
5. Style packages for reusable visual direction.
6. Points, tasks, review, and marketplace mechanics.

## Implementation Phases

### Phase 1: Working Creation Loop

- Register, login, and current user profile.
- Tag tree and prompt assembly.
- txt2img generation through a WebUI-compatible API.
- Save generated image files and create artwork records.
- Frontend workbench for prompt, model, sampler, size, seed, and high-res fix.

### Phase 2: Creative Assets

- Style package CRUD and version history.
- Prompt history and favorites.
- Artwork search and structured tag metadata.
- Point account and point transactions.

### Phase 3: Community and Operations

- Style package marketplace.
- Enterprise tasks and creator applications.
- Content audit workflows.
- Admin dashboards and platform metrics.

## Technical Decisions

- Use Spring Boot 3.2.3 rather than Spring Boot 4.x for current ecosystem stability.
- Use MyBatis-Plus because the old implementation already proved this path more practical than the JPA plan.
- Keep SD integration provider-oriented so Forge, AUTOMATIC1111-compatible APIs, and future ComfyUI adapters can coexist.
