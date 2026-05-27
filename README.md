# 🚀 Release Pipeline Tracker API

> A simplified REST API inspired by **AutoRABIT's** DevOps release automation platform.
> Tracks software releases from creation through deployment using a structured stage pipeline.


[//]: # (## 💡 Project Story &#40;For Interviews&#41;)

[//]: # ()
[//]: # (> *"I built this project after researching AutoRABIT's product. AutoRABIT automates Salesforce)

[//]: # (> release pipelines — so I built a simplified version of that core concept: create a release,)

[//]: # (> assign tasks, advance through stages with business rule validation, and generate a summary report.)

[//]: # (> It gave me a hands-on understanding of the domain I'd be working in."*)

---

## 🏗️ Architecture

```
Client (Postman)
    ↓ HTTP Basic Auth
SecurityFilterChain (Spring Security)
    ↓
ReleaseController / TaskController
    ↓
ReleaseService / TaskService   ← Business Logic + Stage Rules
    ↓
ReleaseRepository / TaskRepository  ← Spring Data JPA
    ↓
H2 (dev) / MySQL (prod)
```

---

## ⚙️ Setup & Run

### Prerequisites
- Java 17
- Maven 3.8+
- (Optional) MySQL for prod profile

### Run (Dev — H2 in-memory)
```bash
git clone <your-repo-url>
cd release-pipeline-tracker
mvn spring-boot:run
```

### Run (Prod — MySQL)
```bash
# Create MySQL database first
mysql -u root -p -e "CREATE DATABASE pipeline_db;"

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod \
  -DDB_USERNAME=root \
  -DDB_PASSWORD=yourpassword
```

### H2 Console (dev only)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:pipelinedb`
- Username: `sa` | Password: *(empty)*

---

## 🔐 Authentication

HTTP Basic Auth — include with every request in Postman:

| Username | Password  | Role  | Access                  |
|----------|-----------|-------|-------------------------|
| admin    | admin123  | ADMIN | Full access              |
| user     | user123   | USER  | GET (read-only) only     |

**In Postman:** Authorization tab → Basic Auth → enter credentials

---

## 📡 API Endpoints

### Releases — `/api/releases`

| Method | Endpoint                        | Description                        | Auth  |
|--------|---------------------------------|------------------------------------|-------|
| POST   | `/api/releases`                 | Create a new release               | ADMIN |
| GET    | `/api/releases`                 | List all releases (paginated)      | ANY   |
| GET    | `/api/releases?stage=PLANNING`  | Filter by stage                    | ANY   |
| GET    | `/api/releases/{id}`            | Get release by ID                  | ANY   |
| GET    | `/api/releases/search?keyword=` | Search releases by name            | ANY   |
| PUT    | `/api/releases/{id}`            | Update release details             | ADMIN |
| PATCH  | `/api/releases/{id}/advance`    | Advance to next stage              | ADMIN |
| PATCH  | `/api/releases/{id}/cancel`     | Cancel release                     | ADMIN |
| GET    | `/api/releases/{id}/summary`    | Generate summary report            | ANY   |
| DELETE | `/api/releases/{id}`            | Delete release                     | ADMIN |

### Tasks — `/api/tasks`

| Method | Endpoint                          | Description                    | Auth  |
|--------|-----------------------------------|--------------------------------|-------|
| POST   | `/api/tasks`                      | Create a task                  | ADMIN |
| GET    | `/api/tasks/{id}`                 | Get task by ID                 | ANY   |
| GET    | `/api/tasks/release/{releaseId}`  | All tasks for a release        | ANY   |
| GET    | `/api/tasks/assignee/{name}`      | Tasks by assignee              | ANY   |
| PUT    | `/api/tasks/{id}`                 | Update task                    | ADMIN |
| PATCH  | `/api/tasks/{id}/status?status=`  | Quick status update            | ADMIN |
| DELETE | `/api/tasks/{id}`                 | Delete task                    | ADMIN |

---

## 🔄 Pipeline Stage Flow

```
PLANNING ──→ IN_PROGRESS ──→ TESTING ──→ COMPLETED
    ↘              ↘             ↘
                        CANCELLED (from any stage)
```

### Business Rules (enforced in ReleaseService)

| Transition               | Rule                                    |
|--------------------------|-----------------------------------------|
| PLANNING → IN_PROGRESS   | Must have at least 1 task               |
| IN_PROGRESS → TESTING    | No tasks with BLOCKED status            |
| TESTING → COMPLETED      | ALL tasks must be DONE                  |
| Any → CANCELLED          | Always allowed (except COMPLETED)       |

---

## 🧪 Sample Postman Flow

```
# 1. Create a release
POST /api/releases
{
  "name": "Payment Integration",
  "version": "v2.1.0",
  "description": "Integrate Razorpay payment gateway",
  "createdBy": "admin"
}

# 2. Add tasks
POST /api/tasks
{
  "title": "Implement payment service",
  "assignee": "darshana",
  "priority": "HIGH",
  "releaseId": 1
}

# 3. Try to advance — should fail (tasks exist check)
PATCH /api/releases/1/advance   → moves to IN_PROGRESS ✓

# 4. Mark tasks done
PATCH /api/tasks/1/status?status=DONE

# 5. Advance to TESTING
PATCH /api/releases/1/advance

# 6. Advance to COMPLETED (only if all tasks DONE)
PATCH /api/releases/1/advance

# 7. View summary report
GET /api/releases/1/summary
```

---

## 🧪 Run Tests

```bash
mvn test
```

Tests cover:
- Release creation with duplicate version check
- Stage advancement business rules
- Blocked task prevention
- Summary percentage calculation
- ResourceNotFoundException on missing ID

---

## 🛠️ Tech Stack

| Technology           | Purpose                                |
|----------------------|----------------------------------------|
| Java 17              | Primary language                       |
| Spring Boot 3.2      | Application framework                  |
| Spring Data JPA      | ORM and database abstraction           |
| Hibernate            | JPA implementation                     |
| Spring Security      | HTTP Basic Auth, role-based access     |
| BCrypt               | Password hashing                       |
| H2 Database          | In-memory DB for development/testing   |
| MySQL                | Production database                    |
| JUnit 5 + Mockito    | Unit testing                           |
| Maven                | Build and dependency management        |
| Lombok               | Boilerplate reduction                  |
| Postman              | API testing and documentation          |
