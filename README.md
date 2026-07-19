# video-processor-service

Listens for `video.upload.completed` events, downloads the video from S3, extracts one frame per second using FFmpeg, packages the frames into a ZIP file, uploads the result back to S3, and publishes processing lifecycle events.

## Technology Stack

- **Java 21** + **Spring Boot 3.5.0**
- **Spring Security** with JWT (JJWT 0.12.6)
- **PostgreSQL 16** with **Flyway** migrations
- **AWS SDK v2** (S3 download/upload) — **LocalStack** for local development
- **FFmpeg** for video frame extraction
- **RabbitMQ** via `rabbit-topic-lib` (choreographed saga)
- **New Relic** APM (Java Agent v8.15.0)
- **SpringDoc OpenAPI** (Swagger UI)
- **JaCoCo** for code coverage (minimum 80%)
- **Hexagonal Architecture** (ports and adapters)

## Responsibility

- Consume `video.upload.completed` from RabbitMQ
- Download the original video from S3
- Run `ffmpeg -i video.mp4 -vf fps=1 frame_%04d.jpg` to extract frames
- Create a ZIP archive with all frames
- Upload the ZIP to S3 at `processed/<userId>/<jobId>/frames.zip`
- Publish `video.processing.started`, `video.processing.completed`, or `video.processing.failed`
- Expose read-only HTTP endpoints for job status

## Architecture

Hexagonal (ports and adapters):

```
infrastructure/rest        -> HTTP layer (ProcessingJobController - read-only)
application/port/input     -> VideoProcessingUseCase interface
application/service        -> VideoProcessingService (use-case implementation)
infrastructure/persistence -> Spring Data JPA + Flyway
infrastructure/s3          -> AWS SDK v2 S3Client adapter (download + upload)
infrastructure/messaging   -> RabbitMQ consumer + publisher (rabbit-topic-lib)
infrastructure/ffmpeg      -> FfmpegVideoProcessorAdapter (external ffmpeg call)
infrastructure/security    -> JwtAuthFilter, SecurityConfig
infrastructure/monitoring  -> NewRelicTracker
```

## API Endpoints

All endpoints require `Authorization: Bearer <JWT>`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/jobs/{jobId}` | Get job by ID |
| `GET` | `/api/v1/jobs` | List jobs for authenticated user |

### Swagger UI

http://localhost:8083/swagger-ui.html

## RabbitMQ Events

**Exchange:** `fiapx.events` (topic)

| Direction | Routing key | Description |
|---|---|---|
| **Consumes** | `video.upload.completed` | Triggers frame extraction |
| **Publishes** | `video.processing.started` | Job picked up, download started |
| **Publishes** | `video.processing.completed` | ZIP uploaded to S3, `resultS3Key` set |
| **Publishes** | `video.processing.failed` | FFmpeg or S3 error, `errorMessage` set |

**Queue:** `processor.video.upload.completed`

## Processing Flow

```
[RabbitMQ] video.upload.completed
        |
        v
1. Create ProcessingJob (status=PENDING)
2. Publish video.processing.started
3. Download video from S3 to temp dir
4. Run: ffmpeg -i <video> -vf fps=1 frame_%04d.jpg
5. ZIP all frames
6. Upload ZIP to S3: processed/<userId>/<jobId>/frames.zip
7. Publish video.processing.completed (resultS3Key set)
8. Clean up temp files
        | on any error
        v
        Publish video.processing.failed (errorMessage set)
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5435/fiapx_processor` | JDBC connection URL |
| `DB_USERNAME` | `fiapx` | Database user |
| `DB_PASSWORD` | `fiapx123` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USER` | `fiapx` | RabbitMQ user |
| `RABBITMQ_PASS` | `fiapx123` | RabbitMQ password |
| `JWT_SECRET` | *(dev key)* | Must match auth-service |
| `AWS_REGION` | `us-east-1` | S3 region |
| `S3_BUCKET_NAME` | `fiapx-videos` | Bucket for source videos and result ZIPs |
| `S3_ENDPOINT` | `http://localhost:4566` | Override for LocalStack |
| `S3_PATH_STYLE` | `true` | Required for LocalStack |
| `AWS_ACCESS_KEY_ID` | — | `test` for LocalStack |
| `AWS_SECRET_ACCESS_KEY` | — | `test` for LocalStack |

## Running Locally

### Prerequisites

- Docker and Docker Compose installed
- Java 21 and Maven installed
- AWS CLI installed
- **FFmpeg** installed on the host machine:
  ```bash
  # macOS
  brew install ffmpeg
  # Ubuntu/Debian
  sudo apt-get install ffmpeg
  ```
  Verify: `ffmpeg -version`
- auth-service running on port 8080 (needed to generate valid JWT tokens)

### 1. Start infrastructure

```bash
cd video-processor-service
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5435` (database `fiapx_processor`, user `fiapx`, password `fiapx123`)
- **RabbitMQ** on port `5672` (AMQP) and `15672` (Management UI — login: `fiapx` / `fiapx123`)
- **LocalStack** (S3 emulator) on port `4566`

> **Tip:** If RabbitMQ and LocalStack are already running from another service (e.g. upload-service), just start the database: `docker-compose up -d postgres`

### 2. Create the S3 bucket and upload a test video

```bash
# Create the bucket (skip if it already exists)
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 mb s3://fiapx-videos

# Upload a test video to simulate what the upload-service would do
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 \
  s3 cp /path/to/your-video.mov s3://fiapx-videos/uploads/test-user/your-video.mov
```

> If you already tested the upload-service, the video is already in the bucket. Check with:
> ```bash
> AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
>   aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 ls s3://fiapx-videos/ --recursive
> ```

### 3. Run the application

**Option A — Maven (terminal):**

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test mvn spring-boot:run
```

**Option B — IDE (IntelliJ / Eclipse):**

Run the main class `VideoProcessorApplication` directly using the Run/Debug button. Add the following environment variables in the Run configuration:
- `AWS_ACCESS_KEY_ID=test`
- `AWS_SECRET_ACCESS_KEY=test`

The application starts on **port 8083**. Flyway automatically runs the database migration on startup.

Verify the service is running:

```
http://localhost:8083/actuator/health
```

### 4. Obtain a JWT token (via auth-service)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"useradmin@email.com","password":"Admin@12345"}' | jq -r '.bearerToken')

USER_ID=$(echo $TOKEN | cut -d'.' -f2 | tr '_-' '/+' | awk '{while(length%4)$0=$0"=";print}' | base64 -d | jq -r '.sub')

echo "TOKEN: $TOKEN"
echo "USER_ID: $USER_ID"
```

## Local Testing — Happy Path

The video-processor-service is event-driven. It consumes `video.upload.completed` events from RabbitMQ, processes the video with FFmpeg, and publishes lifecycle events.

### Test 1 — List jobs (empty)

```bash
curl -s http://localhost:8083/api/v1/jobs \
  -H "Authorization: Bearer $TOKEN" | jq
```

Expected: `[]`

### Test 2 — Publish a `video.upload.completed` event

Open the RabbitMQ Management UI at `http://localhost:15672` → **Exchanges** → `fiapx.events` → **Publish message**.

- **Routing key:** `video.upload.completed`
- **Properties:** `content_type` = `application/json`
- **Payload** (replace `<USER_ID>` with the UUID from step 4, and `s3Key` with the actual path from `s3 ls`):

```json
{
  "uploadId": "b2c3d4e5-5555-6666-7777-888899990000",
  "userId": "<USER_ID>",
  "filename": "your-video.mov",
  "s3Key": "uploads/test-user/your-video.mov",
  "mimeType": "video/quicktime",
  "uploadedAt": "2026-07-06T12:00:00"
}
```

### Test 3 — Observe the processing in the logs

The console should show the full processing pipeline:

```
Received upload.completed event for upload: b2c3d4e5-...
Downloading video from S3: uploads/test-user/your-video.mov
Extracting frames from video: /tmp/fiapx-processor-.../your-video.mov
Uploading result to S3: processed/<USER_ID>/<JOB_ID>/frames.zip
Video processing completed for job: <JOB_ID>
```

The service publishes two events to RabbitMQ:
1. `video.processing.started` — when the job begins
2. `video.processing.completed` — when the ZIP is uploaded to S3

If the status-service and notification-service are running, they will consume these events automatically.

### Test 4 — Verify the job via API

```bash
curl -s http://localhost:8083/api/v1/jobs \
  -H "Authorization: Bearer $TOKEN" | jq
```

Expected: array with 1 item, `"status": "COMPLETED"`, `resultS3Key` populated.

### Test 5 — Verify the result ZIP in S3

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 \
  s3 ls s3://fiapx-videos/processed/ --recursive
```

Expected: a `frames.zip` file under `processed/<USER_ID>/<JOB_ID>/`.

You can download and inspect the ZIP:

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 \
  s3 cp s3://fiapx-videos/processed/<USER_ID>/<JOB_ID>/frames.zip /tmp/frames.zip

unzip -l /tmp/frames.zip
```

Expected: files like `frame_0001.jpg`, `frame_0002.jpg`, etc. (one per second of video).

### Test 6 — Test without a token (should return 401/403)

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/v1/jobs
```

Expected: `401` or `403`.

### Swagger UI

Access: `http://localhost:8083/swagger-ui.html`

Click **Authorize** and enter `Bearer <your-token>` to test the endpoints.

### Summary of tested scenarios

| Step | What it validates |
|---|---|
| GET jobs (empty) | Controller + Auth + DB working |
| `video.upload.completed` event | RabbitMQ consumer receives the event |
| S3 download | Connection to LocalStack works |
| FFmpeg frame extraction | FFmpeg installed and running correctly |
| ZIP created and uploaded to S3 | Result upload works |
| `started` + `completed` events published | Choreographed saga works |
| GET jobs (with result) | Job persisted with status COMPLETED |
| ZIP in S3 | Actual file with video frames |

## Automated Tests

```bash
mvn test
```

JaCoCo enforces **>= 80% instruction coverage**. Coverage report: `target/site/jacoco/index.html`.

## CI/CD

GitHub Actions workflow: build -> test -> SonarCloud -> GHCR push -> EKS deploy.

The `GITHUB_TOKEN` secret is required in CI to download `rabbit-topic-lib` from GitHub Packages. Docker image is **public** on GHCR.

## Database

- PostgreSQL 16, schema: `fiapx_processor`
- Migrations managed by Flyway (`src/main/resources/db/migration`)

## Acknowledgments

This project was developed with the assistance of [Claude](https://claude.com/claude-code) (Anthropic) as an AI pair-programming tool for code implementation, debugging, and documentation.
