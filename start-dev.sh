#!/bin/bash

# Start infrastructure
echo "Starting Docker containers..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 10

# Check if MySQL is ready
until mysqladmin ping -h"localhost" -P"3306" -u"root" -p"pass1" --silent; do
  echo "Waiting for MySQL..."
  sleep 2
done

# Start microservices
echo "Starting Microservice A..."
cd microservice_a
mvn quarkus:dev &
CD_A=$!

echo "Starting Microservice B..."
cd ../microservice_b
mvn quarkus:dev &
CD_B=$!

echo "Starting Microservice C..."
cd ../microservice_c
mvn quarkus:dev &
CD_C=$!

# Wait for user interrupt
echo "All services started. Press Ctrl+C to stop..."
wait

# Cleanup
kill $CD_A $CD_B $CD_C
docker-compose down
