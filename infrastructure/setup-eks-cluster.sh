#!/bin/bash
set -e

# Configuration
CLUSTER_NAME="spring-kafka-eks"
AWS_REGION="us-east-1"
NODE_TYPE="t3.medium"
NODE_COUNT=3
EKS_VERSION="1.28"  # Updated to a supported version

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Starting EKS Cluster Creation ===${NC}"

# Verify AWS credentials
echo -e "${YELLOW}Verifying AWS credentials...${NC}"
if ! aws sts get-caller-identity &> /dev/null; then
    echo "Error: AWS credentials not configured. Please run 'aws configure' first."
    exit 1
fi

# Install eksctl if not present
if ! command -v eksctl &> /dev/null; then
    echo "Installing eksctl..."
    curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
    sudo mv /tmp/eksctl /usr/local/bin
fi

# Create EKS cluster
echo -e "${YELLOW}Creating EKS cluster (version $EKS_VERSION)...${NC}"
eksctl create cluster \
  --name $CLUSTER_NAME \
  --version $EKS_VERSION \
  --region $AWS_REGION \
  --nodegroup-name standard-workers \
  --node-type $NODE_TYPE \
  --nodes $NODE_COUNT \
  --nodes-min 1 \
  --nodes-max 4 \
  --ssh-access \
  --managed \
  --full-ecr-access \
  --asg-access \
  --external-dns-access \
  --node-private-networking

# Update kubeconfig
echo -e "${YELLOW}Updating kubeconfig...${NC}"
aws eks update-kubeconfig --name $CLUSTER_NAME --region $AWS_REGION

# Test cluster access
echo -e "${YELLOW}Testing cluster access...${NC}"
kubectl get nodes

echo -e "\n${GREEN}=== EKS Cluster Setup Complete! ==="
echo -e "Cluster Name: $CLUSTER_NAME"
echo -e "Region: $AWS_REGION"
echo -e "Kubernetes Version: $EKS_VERSION"
echo -e "Nodes: $NODE_COUNT x $NODE_TYPE"
echo -e "To access the cluster, use: kubectl get nodes${NC}"