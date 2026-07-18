# Social

A donation-overlay platform for streamers. Viewers submit donations through a public
link, the streamer marks them as paid, and the donation appears on a browser overlay
with text-to-speech narration. Supports an admin portal, a streamer portal, and a
pluggable TTS provider layer (ElevenLabs default, OpenAI optional).

## Features

- **Public donation page** — viewers donate via a per-streamer donation token, no account needed.
- **Streamer portal** — view donations, summary stats, replay donations, and configure the overlay position.
- **Admin portal** — list/paginate all streamer accounts.
- **Browser overlay** — polls the backend for paid donations and plays TTS audio.
- **TTS providers** — ElevenLabs (default) or OpenAI, selected via config. A noop fallback skips TTS when no provider is configured.
- **Auth** — JWT-based signup/login/logout with active-token revocation.
- **Secrets** — Infisical integration for runtime secret injection in Docker.

## Tech stack

| Layer    | Technology                                                      |
|----------|-----------------------------------------------------------------|
| Backend  | Spring Boot 4, Java 23, Spring Web, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16 (H2 in-memory for tests)                          |
| Migrations | Flyway                                                       |
| Auth     | JWT (jjwt), BCrypt, active-token registry                       |
| Frontend | React 18, React Router 6, TypeScript, Vite                     |
| TTS      | ElevenLabs REST API, OpenAI Audio API (via `RestClient`)       |
| Infra    | Docker Compose, Nginx reverse proxy, Infisical CLI             |

## Repository layout

```
.
├── src/main/java/com/beam/social
│   ├── config/          # @ConfigurationProperties records + SecurityConfig
│   ├── controller/      # REST controllers (auth, donations, overlay, admin, streamer)
│   ├── service/         # AiReaderService implementations + domain services
│   ├── model/           # JPA entities, enums, request/response DTOs
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT filter
│   └── SocialApplication.java
├── src/main/resources
│   ├── application.properties
│   └── db/migration/    # Flyway V1..V9 migrations
├── frontend/           # React + Vite SPA, served by Nginx in Docker
├── docker-compose.yml
├── Dockerfile          # Backend image (build + Infisical runtime)
└── script.sh           # Entrypoint: `infisical run -- java -jar app.jar`
```

## Configuration

All runtime config is read from environment variables via `application.properties`.

### Database

| Variable          | Description                          |
|-------------------|--------------------------------------|
| `APP_JDBC_URL`    | JDBC URL for the app datasource      |
| `APP_FLYWAY_URL`  | JDBC URL used by Flyway              |
| `APP_DB_USER`     | DB username                          |
| `APP_DB_PASSWORD` | DB password                          |

### Auth / admin

| Variable            | Description                              |
|---------------------|------------------------------------------|
| `APP_JWT_SECRET`    | JWT signing secret                       |
| `APP_JWT_EXPIRATION_MS` | Token lifetime (default `86400000`)   |
| `APP_ADMIN_USERNAME` | Bootstrap admin username               |
| `APP_ADMIN_PASSWORD` | Bootstrap admin password               |

### TTS

The active provider is chosen with `APP_TTS_PROVIDER` (`elevenlabs` or `openai`).
A provider's bean is only created when its API key is non-empty, otherwise the noop
fallback marks donations as `SKIPPED`.

| Variable                  | Default                 | Description                              |
|---------------------------|-------------------------|------------------------------------------|
| `APP_TTS_PROVIDER`        | `elevenlabs`            | Active TTS provider                      |
| `APP_ELEVENLABS_API_KEY`  |                         | ElevenLabs API key                       |
| `APP_ELEVENLABS_VOICE_ID` |                         | Default ElevenLabs voice ID              |
| `APP_ELEVENLABS_MODEL_ID` |                         | ElevenLabs model ID (e.g. `eleven_multilingual_v2`) |
| `APP_ELEVENLABS_BASE_URL` |                         | ElevenLabs API base URL                  |
| `APP_OPENAI_API_KEY`      |                         | OpenAI API key                           |
| `APP_OPENAI_VOICE`        | `alloy`                 | Default OpenAI voice                     |
| `APP_OPENAI_MODEL`        | `tts-1`                 | OpenAI TTS model (`tts-1`, `tts-1-hd`)   |
| `APP_OPENAI_BASE_URL`     | `https://api.openai.com`| OpenAI API base URL                      |

A donation may override the default voice via the optional `voiceProfile` field in
the create-donation request; it is passed as the voice id/voice for the chosen provider.

### Infisical (Docker runtime)

| Variable             | Default                          | Description                          |
|----------------------|----------------------------------|--------------------------------------|
| `INFISICAL_TOKEN`    |                                  | Service token for secret injection   |
| `INFISICAL_API_URL`  | `https://app.infisical.com/api`  | Infisical API URL                    |
| `INFISICAL_SECRET_ENV` | `dev`                          | Infisical environment to load        |

## REST API

All endpoints are prefixed with `/api/v1` except auth (`/auth`).

### Auth

| Method | Path           | Auth          | Description                |
|--------|----------------|---------------|----------------------------|
| POST   | `/auth/signup` | public        | Create a streamer account  |
| POST   | `/auth/login`  | public        | Login, returns JWT + tokens |
| POST   | `/auth/logout` | Bearer JWT    | Revoke the active token    |

### Donations

| Method | Path                              | Auth          | Description                              |
|--------|-----------------------------------|---------------|------------------------------------------|
| GET    | `/api/v1/donations/streamer`      | public (`token`) | Resolve a streamer by donation token |
| POST   | `/api/v1/donations`              | public        | Create a donation (`donationToken`)      |
| POST   | `/api/v1/donations/{id}/mark-paid`| public        | Mark a donation paid, enqueue overlay + TTS |

### Overlay

| Method | Path                              | Auth             | Description                          |
|--------|-----------------------------------|------------------|--------------------------------------|
| GET    | `/api/v1/overlay/config`          | public (`token`) | Overlay settings for a streamer      |
| GET    | `/api/v1/overlay/events`          | public (`token`) | Poll new donation overlay events    |
| GET    | `/api/v1/overlay/tts/{donationId}`| public           | Download synthesized TTS audio (mp3) |

### Streamer portal (Bearer JWT)

| Method | Path                                  | Description                          |
|--------|---------------------------------------|--------------------------------------|
| GET    | `/api/v1/streamer/donations`           | List donations (paginated)           |
| GET    | `/api/v1/streamer/donations/summary`   | Aggregate donation summary          |
| POST   | `/api/v1/streamer/donations/{id}/replay`| Re-trigger overlay + TTS for a donation |
| GET    | `/api/v1/streamer/overlay-settings`    | Get overlay position                |
| PUT    | `/api/v1/streamer/overlay-settings`    | Update overlay position             |
| GET    | `/api/v1/streamer/overlay-url`         | Get the streamer's overlay URL       |

### Admin (Bearer JWT, role `ADMIN`)

| Method | Path                        | Description                       |
|--------|-----------------------------|-----------------------------------|
| GET    | `/api/v1/admin/streamers`   | List all streamers (paginated)    |

## TTS architecture

`AiReaderService` is the single abstraction used by `DonationService` to synthesize
audio for a paid donation and persist it via `TtsAudioRepository`. Implementations:

- `ElevenLabsAiReaderService` — active when `APP_TTS_PROVIDER=elevenlabs` and an
  ElevenLabs API key is set. Calls `POST /v1/text-to-speech/{voiceId}`.
- `OpenAiTtsService` — active when `APP_TTS_PROVIDER=openai` and an OpenAI API key
  is set. Calls `POST /v1/audio/speech`.
- `NoopAiReaderService` — fallback when no provider bean exists; marks the donation
  `SKIPPED` so the flow stays consistent.

Donation status transitions (`TtsStatus`): `PENDING → QUEUED → PROCESSING → COMPLETED`
(or `FAILED`, retryable back to `QUEUED`; `SKIPPED` for noop).

Speech text is built in `TtsSpeechFormatter` as `"<amount> from <sender>, '<message>'"`.

## Running locally

### Prerequisites

- JDK 23+
- Node 22+ (for frontend dev)
- Docker + Docker Compose (for the full stack)
- PostgreSQL 16 (or use the compose service)

### Full stack with Docker Compose

1. Copy `.env.example` to `.env` and fill in `INFISICAL_TOKEN` (and any local DB overrides).
2. Build and start everything:

   ```sh
   docker compose up --build
   ```

3. Frontend → http://localhost, backend → http://localhost:8080, Postgres → localhost:5432.

### Backend only (dev)

```sh
./mvnw spring-boot:run
```

Ensure `APP_JDBC_URL`, `APP_DB_USER`, `APP_DB_PASSWORD`, `APP_JWT_SECRET`,
`APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, and the TTS variables are set in your
environment (or via `.env` + Infisical).

### Frontend only (dev)

```sh
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` and `/auth` to `http://localhost:8080`.

## Testing

### Backend

```sh
./mvnw test
```

Tests use H2 with a dedicated Flyway test-migration location (`src/test/resources/db/test-migration`).
The ElevenLabs autoconfiguration is excluded in `src/test/resources/application.properties`,
so `NoopAiReaderService` is active unless a test injects a mock `AiReaderService`.

### Frontend

```sh
cd frontend
npm test
```

Runs Vitest with the React Testing Library.

## Database migrations

Flyway migrations live in `src/main/resources/db/migration` and run automatically on
startup. The current schema version is `V9` (adds `overlay_position` to users).
Test-only migrations live in `src/test/resources/db/test-migration`.