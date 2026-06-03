# ==========================================
# BUILD STAGE
# ==========================================
FROM node:20-alpine AS builder
WORKDIR /app

# Cache NPM packages for rapid rebuilds
COPY package*.json ./
RUN npm install --only=production


# ==========================================
# RUN STAGE
# ==========================================
FROM node:20-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy dependencies and source from builder
COPY --from=builder /app/node_modules ./node_modules
COPY . .

EXPOSE 8083

# Run the node process directly
ENTRYPOINT ["node", "server.js"]
