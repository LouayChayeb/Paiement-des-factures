#!/bin/bash

# Create k8s directory if it doesn't exist
mkdir -p k8s

# Automatically detect microservices in current folder (any folder starting with "ms-")
services=$(ls -d ms-*/ | sed 's:/$::')

for service in $services; do
    echo "Generating Kubernetes manifests for ${service}..."

    # Generate Deployment YAML
    cat > k8s/${service}-deployment.yaml <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${service}
  labels:
    app: ${service}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${service}
  template:
    metadata:
      labels:
        app: ${service}
    spec:
      containers:
      - name: ${service}
        image: ${service}:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
EOF

    # Generate Service YAML (internal ClusterIP)
    cat > k8s/${service}-service.yaml <<EOF
apiVersion: v1
kind: Service
metadata:
  name: ${service}
spec:
  selector:
    app: ${service}
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
EOF

    echo "Done: ${service}-deployment.yaml and ${service}-service.yaml"
done

echo "All Kubernetes manifests generated successfully in k8s/ directory"

