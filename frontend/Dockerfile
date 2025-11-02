# Stage 1: Build the application
FROM node:20-alpine AS build

# Set the working directory
WORKDIR /app

# Copy package files and lockfile
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy the rest of the application
COPY . .

# Build the application with production configuration
RUN npm run build

# Stage 2: Serve the application
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy the build output to replace the default nginx contents
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html/

# Copy the nginx configuration
COPY nginx-custom.conf /etc/nginx/conf.d/default.conf

# Ensure proper permissions
RUN chmod -R 755 /usr/share/nginx/html/

# Set proper permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html && \
    chmod 644 /etc/nginx/conf.d/default.conf

# Expose port 80 (standard HTTP port)
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
