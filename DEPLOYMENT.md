# Cloud deployment

Twibo is packaged as a stateless OCI container and can run on any service that
supports containers. Production deployments use MySQL and the Spring profile
`production`. Login sessions are stored in MySQL, so the service can run multiple
replicas without load-balancer session affinity.

## Runtime contract

| Setting | Required | Purpose |
|---|---:|---|
| `SPRING_PROFILES_ACTIVE=production` | Yes | Enables migrations and cloud-safe defaults |
| `DATABASE_URL` | Yes | MySQL JDBC URL |
| `DATABASE_USERNAME` | Yes | Dedicated application database user |
| `DATABASE_PASSWORD` | Yes | Database password; inject from a secret store |
| `PORT` | No | HTTP port, default `8080` |
| `DATABASE_POOL_SIZE` | No | Maximum DB connections per replica, default `10` |
| `JAVA_TOOL_OPTIONS` | No | Additional JVM flags |

Example JDBC URL:

```text
jdbc:mysql://database-host:3306/twibo?sslMode=REQUIRED&serverTimezone=UTC
```

The application runs Flyway migrations during startup and then validates the JPA
model. Only one replica needs to perform a new migration; Flyway coordinates
concurrent starts with its schema-history lock.

Health endpoints:

- Readiness: `/actuator/health/readiness`
- Liveness: `/actuator/health/liveness`
- Overall health, including the database: `/actuator/health`

## Test the production stack locally

Docker Desktop or another Docker Compose implementation is required.

```powershell
docker compose up --build
```

Open <http://localhost:8080>. Stop it with `docker compose down`. Add `--volumes`
only when you intentionally want to erase the local MySQL data.

## Azure Container Apps

Recommended resources:

- Azure Container Registry (ACR)
- Azure Container Apps
- Azure Database for MySQL Flexible Server
- Container Apps secrets or Azure Key Vault

Create the MySQL server and an empty database named `twibo`. Prefer private
networking; place the Container Apps environment where it can reach the database.
Use a dedicated MySQL user that has privileges only on the `twibo` database.

Build the image in ACR:

```powershell
$ResourceGroup = "twibo-production"
$Registry = "YOUR_UNIQUE_ACR_NAME"
$ImageTag = "1.0.0"

az acr build `
  --resource-group $ResourceGroup `
  --registry $Registry `
  --image "twibo:$ImageTag" `
  .
```

Create or update a Container App with:

- Image: `<registry>.azurecr.io/twibo:<tag>`
- External ingress enabled
- Target port: `8080`
- Minimum replicas: `1` if cold starts are undesirable
- Readiness probe: HTTP GET `/actuator/health/readiness` on port `8080`
- Liveness probe: HTTP GET `/actuator/health/liveness` on port `8080`
- Plain environment values: `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`,
  `DATABASE_USERNAME`
- Secret-backed environment value: `DATABASE_PASSWORD`

Example environment values:

```text
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:mysql://YOUR_SERVER.mysql.database.azure.com:3306/twibo?sslMode=REQUIRED&serverTimezone=UTC
DATABASE_USERNAME=twibo_app
DATABASE_PASSWORD=<secret reference>
```

Use a managed identity to pull from ACR. Database credentials can initially be
stored as Container Apps secrets; Key Vault plus managed identity is the stronger
long-term option.

## AWS App Runner

Recommended resources:

- Amazon Elastic Container Registry (ECR)
- AWS App Runner
- Amazon RDS for MySQL
- AWS Secrets Manager or Systems Manager Parameter Store
- An App Runner VPC connector

Create an RDS MySQL database and an empty schema named `twibo`. Keep RDS private.
Create an App Runner VPC connector using private subnets and a dedicated security
group, then permit inbound MySQL traffic on port `3306` to RDS only from that
connector security group.

Build and push the image:

```powershell
$Region = "us-west-2"
$AccountId = aws sts get-caller-identity --query Account --output text
$Repository = "$AccountId.dkr.ecr.$Region.amazonaws.com/twibo"

aws ecr create-repository --repository-name twibo --region $Region
aws ecr get-login-password --region $Region |
  docker login --username AWS --password-stdin "$AccountId.dkr.ecr.$Region.amazonaws.com"
docker build --tag "${Repository}:1.0.0" .
docker push "${Repository}:1.0.0"
```

In App Runner, create an image-based service and configure:

- ECR image: the image pushed above
- Container port: `8080`
- Health-check protocol: HTTP
- Health-check path: `/actuator/health/readiness`
- VPC connector: the connector that can reach RDS
- Plain values: `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`, `DATABASE_USERNAME`
- Secret reference: `DATABASE_PASSWORD`

App Runner supplies `PORT`; do not create a custom variable named `PORT`.
Grant the App Runner access role permission to pull the ECR image and grant its
instance role permission to read only the specific database secret.

## Production checklist

- Use HTTPS-only ingress and a custom domain.
- Keep the database private and require TLS.
- Store passwords in the provider's secret manager, never in image layers or Git.
- Size the connection pool so `replicas × DATABASE_POOL_SIZE` remains below the
  managed database connection limit.
- Configure database backups and point-in-time recovery.
- Forward application logs to the provider logging service and add alerts for
  failed health checks and elevated HTTP 5xx rates.
- Build a new immutable image tag for every release; do not deploy `latest`.
- Test migrations against a restored database backup before important releases.
