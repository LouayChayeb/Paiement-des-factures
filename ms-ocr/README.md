Bill Processing & OCR Microservice built with Python Flask.

## Features

- 📄 PDF and image OCR processing
- 🔍 Automatic bill information extraction
- 🔗 Integration with Java backend
- 🏥 Spring Boot-like health checks
- 🐳 Docker support
- 📊 Structured logging
- ☁️ Eureka discovery support (optional)

## Prerequisites

- Python 3.11+
- Tesseract OCR
- Poppler (for PDF processing)

## Installation

### Local Setup

1. Install Tesseract OCR:
   ```bash
   # Ubuntu/Debian
   sudo apt-get install tesseract-ocr tesseract-ocr-fra poppler-utils
   
   # macOS
   brew install tesseract tesseract-lang poppler
   
   # Windows
   # Download from: https://github.com/UB-Mannheim/tesseract/wiki
   ```

2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Configure environment:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. Run the application:
   ```bash
   python app.py
   ```

### Docker Setup

1. Build the image:
   ```bash
   docker build -t ms-bill-ocr:1.0.0 .
   ```

2. Run with Docker Compose:
   ```bash
   docker-compose up -d
   ```

## API Endpoints

### Health & Info
- `GET /` - Service information
- `GET /actuator/health` - Health check (Spring Boot compatible)
- `GET /actuator/info` - Service metadata

### Business Endpoints
- `POST /api/bill/upload` - Upload and process bill
  - Form-data: `file` (PDF or image)
  - Query param: `includeRaw=true` (optional, includes raw OCR text)

## Testing

```bash
# Test health endpoint
curl http://localhost:5000/actuator/health

# Upload a bill
curl -X POST http://localhost:5000/api/bill/upload \
  -F "file=@/path/to/bill.pdf"
```

## Configuration

All configuration is done via environment variables (see `.env` file):

| Variable | Default | Description |
|----------|---------|-------------|
| SERVICE_NAME | ms-bill-ocr | Service name |
| SERVICE_PORT | 5000 | Service port |
| JAVA_API_URL | http://localhost:8083/... | Java backend URL |
| REGISTER_WITH_EUREKA | false | Enable Eureka registration |
| LOG_LEVEL | INFO | Logging level |

## Logs

Logs are stored in the `logs/` directory with automatic rotation (10MB max, 10 backups).

## Architecture

This microservice follows Spring Boot conventions:
- `/actuator/*` endpoints for monitoring
- Structured logging
- Health checks
- Service discovery support
- Environment-based configuration

## License

MIT