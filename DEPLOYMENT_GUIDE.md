# Backend Deployment Guide (Localhost & Free Cloud Hosting)

This guide provides step-by-step instructions for running the Spring Boot Chat Backend locally or deploying it on completely free cloud tiers.

---

## Option 1: Localhost Deployment

Running locally is the fastest way to demo and develop alongside Flutter.

### Prerequisites
- **Java 21** installed (`java -version`)
- **PostgreSQL** installed locally or running via **Docker**

---

### Step 1: Start PostgreSQL

#### Method A: Using Docker (Recommended — 1 command)
If you have Docker Desktop installed, run:
```bash
docker run --name chat-postgres -e POSTGRES_DB=chat_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine
```

#### Method B: Local PostgreSQL Service
If you installed PostgreSQL via the official installer:
1. Open `pgAdmin` or `psql`:
   ```bash
   psql -U postgres
   ```
2. Create the database:
   ```sql
   CREATE DATABASE chat_db;
   ```

---

### Step 2: Build & Run Spring Boot

#### Using Maven Wrapper (Direct Run)
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

#### Or Package as Executable JAR:
```powershell
# Build runnable JAR
.\mvnw.cmd clean package -DskipTests

# Run JAR
java -jar target/chat-0.0.1-SNAPSHOT.jar
```

---

### Step 3: Verify It's Running
Open your browser and navigate to:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Test**: [http://localhost:8080/api/chats](http://localhost:8080/api/chats) *(requires `X-User-Id: 1` header)*
- **WebSocket Endpoint**: `ws://localhost:8080/ws`

---

## Option 2: 100% Free Cloud Deployment

You can deploy the backend and database online for free using **Neon** (Database) and **Render** (Spring Boot Web Service).

```
┌─────────────────────────┐          ┌───────────────────────────┐
│     Neon (PostgreSQL)   │  ◄─────  │  Render (Spring Boot API) │
│  Free 0.5 GB Serverless │          │     Free Web Service      │
└─────────────────────────┘          └───────────────────────────┘
```

---

### Step 1: Create Free PostgreSQL Database on Neon
1. Go to [Neon.tech](https://neon.tech) and sign up for a free account.
2. Create a new project named `chat-backend`.
3. Set the database name to `chat_db`.
4. Copy the connection string provided by Neon. It will look like:
   ```text
   postgresql://alex:AbCdEf123@ep-cool-sample.us-east-2.aws.neon.tech/chat_db?sslmode=require
   ```

---

### Step 2: Add Dockerfile to Project Root

To let cloud platforms build your Java 21 app reliably, create a `Dockerfile` in `f:\demo\Dockerfile`:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p storage/chat
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Step 3: Deploy on Render (Free Web Service)
1. Push your project code to GitHub.
2. Go to [Render.com](https://render.com) and create a free account.
3. Click **New +** -> **Web Service**.
4. Connect your GitHub repository.
5. Set the settings:
   - **Environment**: `Docker`
   - **Region**: Select closest to your Neon database
   - **Instance Type**: `Free`
6. Add the following **Environment Variables**:
   | Key | Value |
   | :--- | :--- |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<neon-host>:5432/chat_db?sslmode=require` |
   | `DB_USERNAME` | `<neon-user>` |
   | `DB_PASSWORD` | `<neon-password>` |
   | `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
7. Click **Create Web Service**.
8. Render will deploy your service and give you a public URL (e.g., `https://chat-backend-xyz.onrender.com`).

---

## Summary of URLs

| Environment | REST Base URL | WebSocket URL | Swagger Documentation |
| :--- | :--- | :--- | :--- |
| **Localhost** | `http://localhost:8080` | `ws://localhost:8080/ws` | `http://localhost:8080/swagger-ui.html` |
| **Cloud (Render)** | `https://your-app.onrender.com` | `wss://your-app.onrender.com/ws` | `https://your-app.onrender.com/swagger-ui.html` |
