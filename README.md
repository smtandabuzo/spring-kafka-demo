# Spring Kafka Demo Deployment to AWS EKS

This guide explains how to deploy the Spring Kafka Demo application to AWS EKS.

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Docker installed and running
3. kubectl installed
4. jq for JSON processing

## EKS Cluster Setup

### 1. Create EKS Cluster

```bash
# Make the setup script executable
chmod +x setup-eks-cluster.sh

# Run the cluster setup
./setup-eks-cluster.sh
```

This will:
- Create an EKS cluster named `spring-kafka-eks` in `us-east-1`
- Set up a node group with 3 `t3.medium` instances
- Configure `kubectl` to use the new cluster

### 2. Verify Cluster Access

```bash
kubectl get nodes
```

## Application Deployment

1. **Update Configuration**
   - Update `deploy-to-aws.sh` with your AWS account ID, region, and EKS cluster name
   - Ensure your Kafka bootstrap servers and certificate paths are correct in `application.yml`

2. **Build and Push Docker Image**
   ```bash
   ./deploy-to-aws.sh
   ```

3. **Set Up Kafka Certificates**
   ```bash
   ./setup-kafka-secrets.sh
   ```

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
