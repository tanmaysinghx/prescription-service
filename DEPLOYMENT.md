# Deployment Guide

## 1. Automated (Continuous Deployment)

The project is configured with GitHub Actions to automatically deploy to EC2 on every push to `main`.

### Required GitHub Secrets
Go to **Settings** -> **Secrets and variables** -> **Actions** and add:

| Secret Name | Value |
|-------------|-------|
| `DOCKER_USERNAME` | Your Docker Hub Username |
| `DOCKER_PASSWORD` | Your Docker Hub Access Token |
| `EC2_HOST` | Public IP address of your EC2 instance |
| `EC2_USERNAME` | SSH username (e.g., `ec2-user` for Amazon Linux, `ubuntu` for Ubuntu) |
| `EC2_SSH_KEY` | The content of your `.pem` private key file |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:port/db?ssl-mode=REQUIRED` |
| `SPRING_DATASOURCE_USERNAME` | Database Username |
| `SPRING_DATASOURCE_PASSWORD` | Database Password |

## 2. Manual Deployment (Fallback)

If the automated pipeline fails or you need to test manually on the server:

1.  **SSH into EC2**:
    ```bash
    ssh -i key.pem user@host
    ```

2.  **Run the Container**:
    ```bash
    docker run -d \
      --name prescription-service \
      -p 8080:8080 \
      --restart always \
      -e SPRING_DATASOURCE_URL="YOUR_URL" \
      -e SPRING_DATASOURCE_USERNAME="YOUR_USER" \
      -e SPRING_DATASOURCE_PASSWORD="YOUR_PASSWORD" \
      <your-docker-username>/prescription-service:latest
    ```
