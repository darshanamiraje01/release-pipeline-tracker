# How to Run — Release Pipeline Tracker

## ❌ Wrong command (what caused your error)
mvn run

## ✅ Correct command
mvn spring-boot:run

## Step-by-step setup

### 1. Make sure Java 17+ is installed
java -version
# Should show: openjdk version "17" or higher

### 2. Make sure Maven is installed
mvn -version
# Should show: Apache Maven 3.x.x

### 3. Navigate into the project folder
cd release-pipeline-tracker

### 4. Run the app (dev mode — H2 in-memory database)
mvn spring-boot:run

### 5. You should see in terminal:
# Started ReleasePipelineTrackerApplication in X seconds
# Server running on http://localhost:8080

### 6. Open H2 database console (optional)
# Browser: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:pipelinedb
# Username: sa
# Password: (leave empty)

### 7. Test in Postman
# All requests need Basic Auth:
#   Username: admin   Password: admin123  → Full access
#   Username: user    Password: user123   → Read only

## If you get "Port 8080 already in use":
# Change port in application.properties:
# server.port=8081
# Then access at http://localhost:8081

## To run with MySQL (prod profile):
mvn spring-boot:run -Dspring-boot.run.profiles=prod
