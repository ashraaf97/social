# Social Buzz MVP (Phase 1)

## What is implemented

- Donation creation API (`POST /api/v1/donations`)
- Mock payment confirmation API (`POST /api/v1/donations/{id}/mark-paid`)
- Overlay polling API for OBS/Streamlabs browser source (`GET /api/v1/overlay/events`)
- Streamer donation history API (`GET /api/v1/streamer/donations`)
- Streamer replay API (`POST /api/v1/streamer/donations/{id}/replay`)
- Basic AI-reader-ready message fields (`voiceProfile`, `ttsStatus`) with no-op AI service

## Backend stack

- Spring Boot 4 + Spring MVC
- Flyway migration in `src/main/resources/db/migration/V1__init_schema.sql`
- PostgreSQL (JDBC + Flyway)

## Database setup (PostgreSQL)

Start local database:

- `docker compose up -d postgres`

Default DB config is now PostgreSQL via `src/main/resources/application.properties`.
You can override credentials/URLs with env vars from `.env.example`.

## Streamer auth (v1 placeholder)

- Streamer portal endpoints require `X-Streamer-Key`
- Default key is configured in `src/main/resources/application.properties`:
  - `app.streamer.portal-key=change-me`

## OBS/Streamlabs overlay URL

Use Browser Source URL:

- `http://localhost:8080/overlay.html?streamerId=streamer-demo`

The page polls `GET /api/v1/overlay/events` every ~1.5 seconds and displays popup alerts.

## Frontend app (React)

Frontend project is in `frontend/` and includes:

- Donate page
- Streamer portal page
- Overlay preview page

Run frontend:

- `cd frontend`
- `npm install`
- `npm run dev`

## Backend run

- `./mvnw spring-boot:run`

## Test status

- `./mvnw clean test` passes.
