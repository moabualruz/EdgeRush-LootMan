# Multi-stage build for Vue 3 + Vite frontend
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for better caching
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build arguments for environment variables (injected at build time)
ARG VITE_API_URL=/api
ARG VITE_WS_URL=
ARG VITE_DISCORD_CLIENT_ID=
ARG VITE_BATTLENET_CLIENT_ID=

# Set environment variables for build
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_WS_URL=$VITE_WS_URL
ENV VITE_DISCORD_CLIENT_ID=$VITE_DISCORD_CLIENT_ID
ENV VITE_BATTLENET_CLIENT_ID=$VITE_BATTLENET_CLIENT_ID

# Build the application
RUN npm run build

# Stage 2: Production
FROM nginx:alpine

# Copy built assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
