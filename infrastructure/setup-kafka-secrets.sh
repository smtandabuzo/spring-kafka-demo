#!/bin/bash

# Create a Kubernetes secret for Kafka certificates
echo "Creating Kubernetes secret for Kafka certificates..."

# Create the secret from the certificate files
kubectl create secret generic kafka-certs \
  --from-file=keystore.jks=src/main/resources/certs/keystore.jks \
  --from-file=truststore.jks=src/main/resources/certs/truststore.jks \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Kafka certificates secret created successfully!"
