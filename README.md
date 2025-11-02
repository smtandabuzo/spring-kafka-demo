# Spring Kafka Demo

A full-stack application demonstrating event streaming with Spring Boot, Kafka, and Angular.

## Features

- **Backend**: Spring Boot application with Kafka integration
- **Frontend**: Angular application with Material UI
- **Event Types**: Page views, add to cart, and purchase events
- **Real-time Processing**: Kafka for event streaming
- **Monitoring**: Kafka UI for cluster monitoring

## Prerequisites

- Docker and Docker Compose
- Java 17+
- Node.js 18+ and npm
- Angular CLI (for development)

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/spring-kafka-demo.git
   cd spring-kafka-demo
   ```

2. **Start the application**
   ```bash
   docker-compose up -d --build
   ```

3. **Access the applications**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Kafka UI: http://localhost:8081

## Project Structure

```
spring-kafka-demo/
├── backend/               # Spring Boot application
├── frontend/              # Angular application
├── docker-compose.yml     # Docker Compose configuration
└── README.md             # This file
```

## Development

### Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend (Angular)

```bash
cd frontend
npm install
ng serve
```

### Environment Variables

Create `.env` file in the root directory:
```
SPRING_PROFILES_ACTIVE=dev
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Docker

### Build and Run

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## Monitoring

Access Kafka UI at http://localhost:8081 to:
- View topics and messages
- Monitor consumer groups
- Inspect schemas
- Check cluster health

## Deployment

### AWS EKS

1. **Prerequisites**
   - AWS CLI configured
   - kubectl installed
   - jq for JSON processing

2. **Deploy to EKS**
   ```bash
   chmod +x setup-eks-cluster.sh
   ./setup-eks-cluster.sh
   ./deploy-to-aws.sh
   ```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

4. **Deploy Kafka using Strimzi**
   ```bash
   kubectl create namespace kafka
   kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
   ```

5. **Verify Deployment**
   ```bash
   kubectl get pods --all-namespaces
   kubectl get svc
   ```

## Accessing the Application

After deployment, get the external IP or hostname of the LoadBalancer service:

```bash
kubectl get svc spring-kafka-demo
```

## Cluster Information

- **Cluster Name**: spring-kafka-eks
- **Kubernetes Version**: 1.28
- **Node Type**: t3.medium
- **Node Count**: 3 (auto-scaling 1-4)
- **Region**: us-east-1

## Troubleshooting

- Check pod logs:
  ```bash
  kubectl logs -f <pod-name> -n <namespace>
  ```

- Check service details:
  ```bash
  kubectl describe svc spring-kafka-demo
  ```

- Check pod status:
  ```bash
  kubectl describe pod <pod-name> -n <namespace>
  ```

## Cleanup

To delete the EKS cluster and all associated resources:

```bash
eksctl delete cluster --name spring-kafka-eks --region us-east-1
```

To delete only the application deployment:

```bash
kubectl delete deployment spring-kafka-demo
kubectl delete svc spring-kafka-demo
kubectl delete secret kafka-certs
kubectl delete -n kafka -f 'https://strimzi.io/install/latest?namespace=kafka'
kubectl delete namespace kafka
```

## Security Notes

- The default configuration uses a public endpoint for the EKS cluster
- For production use, consider:
  - Enabling private endpoint access
  - Restricting IAM permissions
  - Using OIDC for service accounts
  - Implementing network policies
