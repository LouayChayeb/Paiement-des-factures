#!/bin/bash

# Automatically detect microservices in current folder (folders starting with "ms-")
services=$(ls -d ms-*/ | sed 's:/$::')

# Generate Dockerfile for each service
for service in $services; do
    cat > ${service}/Dockerfile <<EOF
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
    echo "Generated Dockerfile for ${service}"
done

echo "All Dockerfiles generated successfully"

