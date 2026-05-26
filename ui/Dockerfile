# Build stage: Compile Vue 3 SPA with Vite
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage: Serve with Nginx
FROM nginx:latest
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx/ui.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
