# Journal App Backend (Spring Boot + MongoDB + JWT)

A secure backend REST API for a personal journal application.  
Users can sign up, log in, create journal entries, update them, delete them, and manage their account.  
Authentication is completely stateless using **JWT (JSON Web Tokens)**.

---

## 🚀 Features

### 🔐 Authentication & Security
- User signup & login
- Password hashing using **BCrypt**
- JWT-based authentication (stateless)
- Role-based access (`USER`, `ADMIN`)
- Custom JWT filter integrated with Spring Security 6
- CORS configured for frontend communication

### 📒 Journal Features
- Create journal entries  
- Update journal entries  
- Delete journal entries  
- Fetch all entries of the logged-in user  
- Backend validates ownership before allowing access

### 👤 User Management
- Update username
- Update password (with old password check)
- Delete account (all journals removed in a single transaction)

### 🛠 Admin Features
- View all users
- Create admin users

### 💾 Database
- MongoDB (Atlas or local)
- - MongoDB Atlas for persistent storage
- Automatic index creation for unique usernames
- Unique index on usernames

---

## 🧰 Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Security 6**
- **Spring Data MongoDB**
- **JWT (jjwt 0.12.x)**
- **Lombok**
- **Maven**

---

## 🌐 CORS Configuration

The backend uses a dynamic CORS setup to allow communication only between trusted frontend domains.

CORS origins are configured using:
app.allowed.origins=http://localhost:5500,http://127.0.0.1:5500,https://mydaily-journal-app.netlify.app

This value is loaded at runtime (locally or via cloud environment variables) and applied in `SpringSecurity`:

- Only whitelisted frontend domains are allowed
- `Authorization` header is allowed for JWT
- `allowCredentials(true)` is enabled for secure token handling
- Supports preflight `OPTIONS` requests

This ensures only your official deployed frontend can communicate with this backend.

---

## 📁 Folder Structure

```
src/
 ├── main/java/com/example/journalapp
 │   ├── config/          → Spring Security configuration
 │   ├── controller/      → REST controllers
 │   ├── dto/             → Request DTOs
 │   ├── entity/          → MongoDB documents
 │   ├── filter/          → JWT Authentication filter
 │   ├── repository/      → Mongo repositories
 │   ├── service/         → Business logic
 │   ├── utils/           → JWT utilities
 │   └── JournalApplication.java  → Main application
 │
 └── main/resources
     ├── application.properties
     └── application-secret.properties  (NOT committed to Git)
```

---

## 🔑 Environment Variables (Stored in `application-secret.properties` or set on the server)

```
MONGO_URI=your-mongodb-connection-string
MONGO_DB=journaldb
JWT_SECRET=your-secret-key
APP_ALLOWED_ORIGINS=http://localhost:5500,https://mydaily-journal-app.netlify.app
```

`application.properties` loads these safely via:

```
spring.data.mongodb.uri=${MONGO_URI}
spring.data.mongodb.database=${MONGO_DB}
jwt.secret=${JWT_SECRET}
app.allowed.origins=${APP_ALLOWED_ORIGINS}
```

---

## ▶️ Running the Project

### **1. Add your environment variables**
Inside `src/main/resources/application-secret.properties`:

```
MONGO_URI=...
MONGO_DB=...
JWT_SECRET=...
```

### **2. Build and run**
```
mvn spring-boot:run
```

Server starts at:

```
http://localhost:8080
```

---

## 📌 API Endpoints

### 🔓 Public Routes
| Method | Endpoint        | Description |
|--------|------------------|-------------|
| POST   | `/public/signup` | Create new user |
| POST   | `/public/login`  | Login & receive JWT |
| GET    | `/public/health-check` | Check service status |

---

## 🔐 User Routes (Require JWT)

### **User Info**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/user` | Get logged-in username |
| PUT | `/user` | Update username or password |
| DELETE | `/user` | Delete user + all journals |

Example request body for update:

```json
{
  "username": "newName",
  "oldPassword": "oldPass123",
  "newPassword": "newPass456"
}
```

---

## 📒 Journal Routes (Require JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/journal` | Get all journals of logged-in user |
| POST | `/journal` | Create new journal entry |
| GET | `/journal/id/{id}` | Get a journal by ID (only if owner) |
| PUT | `/journal/id/{id}` | Edit journal |
| DELETE | `/journal/id/{id}` | Delete journal |

Example create request:

```json
{
  "title": "My Day",
  "content": "Today was productive!"
}
```

---

## 🛠 Admin Routes (Role: ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/all-users` | View all users |
| POST | `/admin/create-admin-user` | Create admin user |

---

## 🔒 JWT Authentication Flow (Short Summary)

1. User logs in → server validates credentials  
2. Server returns a signed JWT (expires in 1 hr)  
3. Frontend stores token (localStorage)  
4. Every protected request must include:

```
Authorization: Bearer <token>
```

5. `JwtFilter` validates token → Spring Security sets Authentication  
6. Controllers can identify logged-in user via:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
```

---

## 🧹 Notes

- Do **NOT** commit `application-secret.properties`
- `.gitignore` already excludes it  
- Your backend is fully stateless and safe to deploy anywhere  
- Deployment section can be added later once frontend + backend go live

---

## 📄 License
MIT License (recommended)

---

## 📬 Contact
Built by **Vedansh Paliwal**  
(Feel free to include GitHub/LinkedIn links)

---
