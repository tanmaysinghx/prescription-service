# Running Prescription Service with Docker

## Prerequisites
- Docker installed
- Image pulled from Docker Hub (or built locally)

## Handling Secrets (.properties)

You should **NOT** put secrets (passwords, keys) in your `application.properties` file inside the image. Instead, use **Environment Variables**. Spring Boot automatically maps environment variables to configuration properties.

### Common Mappings

| Property | Environment Variable |
|----------|----------------------|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` |

### Unset Properties
For `spring.jpa.hibernate.ddl-auto` or other properties, you can also set them via env vars: `SPRING_JPA_HIBERNATE_DDL_AUTO=update`.

## Running the Container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/db_name \
  -e SPRING_DATASOURCE_USERNAME=your_user \
  -e SPRING_DATASOURCE_PASSWORD=your_secret_password \
  --name prescription-service \
  <your-docker-username>/prescription-service:latest
```

## GitHub Actions Secrets

To enable the CI/CD pipeline to push images, go to your GitHub Repository -> Settings -> Secrets and Variables -> Actions and add:

- `DOCKER_USERNAME`: Your Docker Hub username
- `DOCKER_PASSWORD`: Your Docker Hub Access Token (or password)
