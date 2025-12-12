# FMS Project

## Scenario

You are required to develop 3 microservices (using Quarkus or any other framework you
prefer) for a fictitious Fleet Management System (FMS) as follows:

● Microservice_A: exposes an HTTP Restful API, that allows performing CRUD operations
for the management of Fleet entities (i.e: Driver, Car, Trip, etc). Also provides endpoints
for assigning a Driver to a Car.

● Microservice_B: simulates a Car that is driven around a city. This microservice generates
heartbeats on frequent time intervals that encapsulate the state of the car (car_id,
geo-coordinates, speed, etc) and driver identity.

● Microservice_C: consumes heartbeats to apply penalty points to drivers that are not
driving in a behaved manner. Two (2) Penalty points are added for every Km over
60Km/h, five (5) points for over 80Km/h. Driver/Penalty point map is stored in a data store
(your choice)

Microservices should communicate amongst themselves using a Kafka-based message bus.


## Solution Approach

The projects are written using Java 21 and Quarkus 3.29.2 through IntelliJ IDE.

Apart the three aforementioned microservices, and the broker, a couple of extra 
components were considered as nice to have. More precisely:

- MySQL version 8, a matured RDBMS technology to store the data.
- Repanda message broker, for project slick communication.
- Keycloak, the authentication and authorization server.
- Jaeger for distributed tracing and monitoring of the projects.
- Kafkaui for monitoring Kafka topic messages.



### Database


The database is used to store the `driver`/`car`/`trip` data and of course the driver `penalties`.

Microservice_A owns and preserves the state three first entities in the database `zimono_trg_a`.

Microservice_C respectively stores the penalties in the database `zimono_trg_c`.


### Messaging


In general tha party begins from Microservice_A which under the beginning of a new trip 
notifies through Kafka (topic: trip_starts) the Microservice_B providing it with the `trip_id`, 
the `car_id` and the car driver, `driver_id`.

For such a signal, `Microservice_B` starts generating in a timely fashion (~10secs) 
the so-called heartbeats and publishes them back to Kafka (topic: car-heartbeats). 
Will only stop sending heartbeats for the trip when `Microservice_A` asks it through 
Kafka (topic: trip-stops).

The heartbeats (topic: car-heartbeats) are consumed by `Microservice_C` with obligation 
to calculate penalties and, if there are any, to store them in the database and to notify
`Microservice_A` of each penalty acknowledgement (topic: driver-penalties).

Finally, when `Microservice_A` get each driver penalty, it updates the driver's new total of 
penalties to the database.

### shared-dtos
Stands for common resources shared between microservices.

Add the common lib to the maven cache:
```aiignore
mvn -f ./shared-dtos/pom.xml clean install
```

### Microservice A (http://localhost:18083)


#### Available Endpoints

```bash
# Drivers (DriverResource.class)
GET    /api/drivers              # List all drivers
GET    /api/drivers/{id}         # Get driver by ID
POST   /api/drivers              # Create driver
PUT    /api/drivers/{id}         # Update driver
DELETE /api/drivers/{id}         # Delete driver

# Cars (CarResource.class)
GET    /api/cars                      # List all cars
GET    /api/cars/search               # Search for cars with pagination, sorting and filtering
GET    /api/cars/{id}                 # Get car by ID
POST   /api/cars                      # Create car
PUT    /api/cars/{id}                 # Update car
DELETE /api/cars/{id}                 # Delete car
POST   /api/cars/assign-driver        # Assign a driver to a car
POST   /api/cars/{id}/unassign-driver # Unassign a driver to a car

# Trips (TripResource.class)
GET    /api/trips                # List all trips
GET    /api/trips/{id}           # Get trip by ID
POST   /api/trips                # Create trip
PUT    /api/trips/{id}           # Update trip
DELETE /api/trips/{id}           # Delete trip
POST   /api/trips/{id}/start     # Start trip
POST   /api/trips/{id}/stop      # Stop trip

# Penalties (PenaltyResource.class proxied to microservice_c)
GET    /api/penalties              # Get all penalties             (TOKEN admin)
GET    /api/penalties/drivers/{id} # Get penalties of a driver     (TOKEN admin/operator)
GET    /api/penalties/trips/{id}   # Get total penalties in a trip (TOKEN admin/operator)

# Health & Monitoring
GET    /health/live              # Liveness probe
GET    /health/ready             # Readiness probe
GET    /metrics                  # Prometheus metrics
```

### Microservice B

No database, only messagging!


### Microservice C (http://localhost:18085)


#### Available Endpoints
The below endpoints are for Microservice_A to be able to access

```bash
# Penalties (PenaltyResource.class)
GET    /api/penalties                    # Get all penalties
GET    /api/penalties/drivers/{driverId} # Get penalties by driver
GET    /api/penalties/trips/{tripId} # Get penalties on trip

# Health & Monitoring
GET    /q/health/live
GET    /q/health/ready
GET    /metrics
```

From the nature of the project, the functionality to the end user is limited to the ones
mentioned above plus Swagger UI (user-friedly api services use and documentation).
The latter is available from the browser at:

  http://localhost:18083/q/swagger-ui/ => ui

  http://localhost:18083/q/openapi => json

where 18083 is the port served by `Microservice_A`.


## Prerequisites

- Docker & Docker Compose
- Java 21
- Maven 3.8.5

## Starting the Infrastructure

Our little platform consists of a few microservices (A, B, C) and a few supporting services
like Kafka (repanda), MySQL, Keycloak, Jaeger and Kafka-ui.

The latter two are used for monitoring and authentication,
while Keycloak for managing users and generating access tokens.

Kafka as the projects' communication mediator (broker) and MySQL for storing the precious data.

### 1. Start All Services

```bash
cd {project_root_path}

# Start all infrastructure services
docker-compose up -d

# Check all infra services are running
docker-compose ps

# Expected services:
# - fms-mysql-dev (MySQL)
# - fms-redpanda-dev (Kafka)
# - fms-kafkaui-dev (Kafka ui)
# - fms-kafkaui-dev (Kafka UI)
# - fms-keycloak-dev (Keycloak)
# - fms-jaeger-dev (Keycloak)


```

### 2. Wait for Services to be Ready

```bash
# Check Keycloak is ready (wait ~30 seconds)
curl -f http://localhost:18080/health/ready

# Check MySQL
docker-compose exec -it mysql mysqladmin ping -h localhost -uroot -ppass1

# Check Redpanda
docker-compose exec redpanda rpk cluster health

# Check Kafkaui
curl -f http://localhost:19002

# Check Jaeger
curl -f http://localhost:16686
```

### 3. Verify Keycloak Realm Imported

While you read the below instructions, it would be wise to

check at the same time the realm configuration file in the `config/keycloak` folder.

```bash
# Open Keycloak admin console
# URL: http://localhost:18080
# Username: admin
# Password: admin

# Verify 'fms' realm exists
# Verify users: admin, operator1, operator2, viewer1, testuser
```

## Starting the Microservices

### Microservice A (Core Service)

```bash
cd microservice_a

# Development mode
mvn quarkus:dev

# Production mode
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar

# Verify
curl --location --request GET 'http://localhost:18083/health/ready'
```

### Microservice B (Car Simulator)

```bash
cd microservice_b

mvn quarkus:dev

# No HTTP endpoint - check logs for heartbeat generation
```

### Microservice C (Penalty Service)

```bash
cd microservice_c

mvn quarkus:dev

# Verify
curl http://localhost:18085/q/health/ready
```

## Testing Keycloak Integration

You are going to need a valid access token to test the DriverResource endpoints.

Don't panic with the commands, these actions and more coexist in the Postman collection as well.

### Get Access Token
```bash
# Using admin user
curl -X POST 'http://localhost:18080/realms/fms/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fms-microservice-a' \
  -d 'client_secret=fms-microservice-a-secret' \
  -d 'username=admin' \
  -d 'password=admin123' \
  -d 'grant_type=password'

# Extract access_token from response
export TOKEN=<access_token>
```

### Test Authenticated Endpoints

```bash
# Get all drivers (requires admin or operator role)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:28080/api/drivers

# Get specific driver
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:28080/api/drivers/1

# Create driver (requires admin role only)
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "drivingLicense": "ABC123"
  }' \
  http://localhost:28080/api/drivers
```



## Available Users

These users are allowed to gain a temporal access token and by using it are able
to access few endpoints. For demo reasons, nearly all the endpoints are unprotected and
only a few are protected to achieve the proof of concept.

| Username   | Password     | Roles                  | Use Case                 |
|------------|--------------|------------------------|--------------------------|
| admin      | admin123     | admin, operator, viewer| Full access              |
| operator1  | operator123  | operator, viewer       | Operational tasks        |
| operator2  | operator456  | operator, viewer       | Operational tasks        |
| viewer1    | viewer123    | viewer                 | Read-only access         |
| testuser   | test123      | operator               | Testing                  |



## Admin Interfaces

| Service       | URL                    | Credentials        |
|---------------|------------------------|--------------------|
| Keycloak      | http://localhost:18080 | admin / admin      |
| Kafka UI      | http://localhost:19002 | No auth            |



## Monitoring Endpoints


## Common Commands

### View Logs

```bash
# Docker services
docker-compose logs -f keycloak
docker-compose logs -f mysql
docker-compose logs -f redpanda
docker-compose logs -f kafkaui

# Microservices (when running with mvn quarkus:dev)
# Logs appear in the terminal
```

### Reset Everything

```bash
# Stop all services
docker-compose down -v

# Start fresh
docker-compose up -d
```

### Kafka Topics

```bash
# List topics
docker-compose exec redpanda rpk topic list

# View messages in topic

(signal to svc B to start sending heartbeats for the trip)
docker-compose exec redpanda rpk topic consume trip-starts

(signal to svc B to stop sending heartbeats for the trip)
docker-compose exec redpanda rpk topic consume trip-stops 

(signal to svc C to calculate penalties for the trip)
docker-compose exec redpanda rpk topic consume car-heartbeats 

(signal svc A to update penalties for the driver )
docker-compose exec redpanda rpk topic consume driver-penalties
```

## Troubleshooting

### Keycloak Not Starting

```bash
# Check logs
docker-compose logs keycloak

# Common issue: MySQL not ready
# Wait longer or restart Keycloak:
docker-compose restart keycloak
```

### 401 Unauthorized

```bash
# Token may have expired (5 minute lifetime)
# Get a new token using the curl command above

# Verify token is valid
curl -X POST 'http://localhost:18080/realms/fms/protocol/openid-connect/token/introspect' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fms-microservice-a' \
  -d 'client_secret=fms-microservice-a-secret' \
  -d "token=$TOKEN"
```
or use relevant request from Postman.



### Port Already in Use

```bash
# Find and kill process using port

lsof -ti:18080 | xargs kill -9
lsof -ti:18083 | xargs kill -9
lsof -ti:18084 | xargs kill -9
lsof -ti:18085 | xargs kill -9
```

## Development Workflow

1. **Start infrastructure**: `docker-compose up -d`
2. **Start microservice_a**: `cd microservice_a && mvn quarkus:dev`
3. **Start microservice_c**: `cd microservice_c && mvn quarkus:dev`
4. **Start microservice_b**: `cd microservice_b && mvn quarkus:dev`
5. **Get auth token** using curl command above
6. **Test endpoints** with Bearer token

You may take a close look also to the projects managing scripts in the project's `root` folder
```aiignore
start_all.sh
stop_all.sh

start_dev.sh
```

And finally you can always use your preferred IDE to run the projects.


### Samples commands


Start services with startup scripts ie.
```
chmod +x start-dev.sh
./start-dev.sh
```

Start services in a more controlled way
```
docker-compose up -d

# while in project directory
mvn quarkus:dev -Dquarkus.profile=dev
```

Observe a project's console log output
```aiignore
docker logs -f --tail 50 fms-microservice-a
```

Select specific compose yaml file to spin
```
docker-compose -f docker-compose-staging.yml up -d
```

Start quarkus in dev mode with specific profile
```
mvn quarkus:dev -Dquarkus.profile=staging
```

Check what kafka knows about each project (group)
```
docker exec -it fms-redpanda-dev rpk group describe microservice_c
```

### Build Native Image and Run
```
mvn -f ./microservice_a/pom.xml clean package -Dquarkus.package.type=native -Dmaven.test.skip=true
ll ./microservice_a/target
./microservice_a/target/microservice_a-1.0-SNAPSHOT-runner

mvn -f ./microservice_b/pom.xml clean package -Dquarkus.package.type=native -Dmaven.test.skip=true
ll ./microservice_b/target
./microservice_b/target/microservice_b-1.0-SNAPSHOT-runner

mvn -f ./microservice_c/pom.xml clean package -Dquarkus.package.type=native -Dmaven.test.skip=true -Dquarkus.native.container-build=true  # if not building in linux, to build a linux binary
ll ./microservice_c/target
./microservice_c/target/microservice_c-1.0-SNAPSHOT-runner
```

jacoco reports
```
./microservice_a
./mvnw clean package -Dnative -DskipTests -Djacoco.skip=true
```