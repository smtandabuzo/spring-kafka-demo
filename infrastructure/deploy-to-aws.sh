#!/bin/bash

# Configuration
AWS_ACCOUNT_ID="810772959397"
AWS_REGION="us-east-1"
ECR_REPO_NAME="spring-kafka-demo"
EKS_CLUSTER_NAME="spring-kafka-eks"
ECR_REPOSITORY_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}"

# Exit on error
set -e

# Create ECR repository (if it doesn't exist)
echo "Checking ECR repository..."
if ! aws ecr describe-repositories --repository-names $ECR_REPO_NAME --region $AWS_REGION &> /dev/null; then
    echo "Creating ECR repository..."
    aws ecr create-repository --repository-name $ECR_REPO_NAME --region $AWS_REGION
fi

# Login to ECR
echo "Logging in to Amazon ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY_URI

# Build and tag the Docker image
echo "Building Docker image..."
docker build -t $ECR_REPO_NAME .

echo "Tagging image for ECR..."
docker tag $ECR_REPO_NAME:latest $ECR_REPOSITORY_URI:latest

# Push the image to ECR
echo "Pushing image to ECR..."
docker push $ECR_REPOSITORY_URI:latest

# Update kubeconfig
echo "Updating kubeconfig..."
aws eks --region $AWS_REGION update-kubeconfig --name $EKS_CLUSTER_NAME

# Create Kubernetes deployment and service
echo "Deploying to EKS..."
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $ECR_REPO_NAME
  labels:
    app: $ECR_REPO_NAME
spec:
  replicas: 2
  selector:
    matchLabels:
      app: $ECR_REPO_NAME
  template:
    metadata:
      labels:
        app: $ECR_REPO_NAME
    spec:
      containers:
      - name: $ECR_REPO_NAME
        image: $ECR_REPOSITORY_URI:latest
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: certs-volume
          mountPath: /app/certs
          readOnly: true
      volumes:
      - name: certs-volume
        secret:
          secretName: kafka-certs
---
apiVersion: v1
kind: Service
metadata:
  name: $ECR_REPO_NAME
spec:
  selector:
    app: $ECR_REPO_NAME
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: LoadBalancer
EOF

echo "Deployment complete!"
# Get the service URL
echo "Getting the service URL..."
kubectl get svc $ECR_REPO_NAME
