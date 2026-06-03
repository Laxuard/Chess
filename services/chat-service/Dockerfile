# ==========================================
# BUILD STAGE
# ==========================================
FROM python:3.11-alpine AS builder
WORKDIR /app

# Install compilation dependencies for cryptography if needed
RUN apk add --no-cache gcc musl-dev libffi-dev openssl-dev cargo

# Cache python requirements for rapid builds
COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# ==========================================
# RUN STAGE
# ==========================================
FROM python:3.11-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy installed python packages from builder
COPY --from=builder /install /usr/local
COPY . .

ENV PYTHONUNBUFFERED=1

EXPOSE 8082

# Start the application
ENTRYPOINT ["python", "app.py"]

