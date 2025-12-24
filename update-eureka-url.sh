#!/bin/bash

# Eureka service URL in Kubernetes
EUREKA_URL="http://ms-registry:8761/eureka"

# Automatically detect microservices (folders starting with ms-)
services=$(ls -d ms-*/ | sed 's:/$::')

for service in $services; do
    yml_file="${service}/src/main/resources/application.yml"

    if [ -f "$yml_file" ]; then
        echo "Updating Eureka URL in ${yml_file}..."
        
        # Replace the old URL with the new one
        sed -i "s|http://localhost:8761/eureka|$EUREKA_URL|g" "$yml_file"

        # Optional: if you want to keep the environment variable wrapper
        sed -i "s|\${EUREKA_URI:.*}|${EUREKA_URL}|g" "$yml_file"

        echo "Done: ${service}"
    else
        echo "Warning: ${yml_file} not found!"
    fi
done

echo "All microservices updated with the correct Eureka URL."

