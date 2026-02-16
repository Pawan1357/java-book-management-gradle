# 📚 Library Management System (Spring Boot)

A Book Management System built using **Spring Boot**, **Spring Security (JWT)**, **JPA/Hibernate**, and **MySQL**

---

## 🚀 Features

### 1️⃣ User Management

- User registration with **email** and **unique library ID**
- Login using **email or library ID**
- Password encryption using **BCrypt**
- JWT-based authentication & authorization
- Role-based access (`ADMIN`, `USER`)

### 2️⃣ Book Management (Admin only)

- Add new books
- Update book details
- Delete books
- View all available books

### 3️⃣ Borrow & Return Books

- Users can borrow **only one book at a time**
- Tracks:
  - Borrow date
  - Due date
  - Return date

- Automatically calculates **late fees** on return

### 4️⃣ Monthly Report Scheduler

Runs **at the end of each month** and generates:

- 📕 Books borrowed in the month
- 📗 Books returned in the month
- ⏰ Overdue books with late fees
- 👤 User activity summary (borrowed vs returned count)

> For development/testing, the scheduler can be temporarily configured to run every minute.

---

## 🧱 Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA
- MySQL
- Gradle
- Lombok

---

## 📂 Project Structure (High Level)

```
library-management-gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/main/java/com/exam/library_management
├── src/main/resources/application.yaml
└── src/test/java/com/exam/library_management
```

## ⚙️ Prerequisites

Make sure you have the following installed:

- **Java 17 or higher**
- **Gradle 8+ (or use Gradle wrapper if generated locally)**
- **MySQL 8+**
- **Git**
- Postman / curl (for API testing)

---

## 🗄️ Database Setup

1. Start MySQL
2. Create a database:

   ```sql
   CREATE DATABASE library_db;
   ```

3. Update credentials in `application.yaml` :

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/library_db
       username: root
       password: your_password
   ```

> Tables will be auto-created by Hibernate on application startup.

---

## ▶️ Running the Application

From the project root:

```bash
./gradlew clean build
./gradlew bootRun
```

The application will start on:

```
http://localhost:8080
```

---

## 🔐 Authentication Flow

### 1. Register User

- `POST /api/auth/register`

### 2. Login

- `POST /api/auth/login`
- Returns a **JWT token**

### 3. Use JWT Token

Add this header to all protected APIs:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## 📘 API Testing Guide

### 👤 User APIs

| Action   | Method | Endpoint             |
| -------- | ------ | -------------------- |
| Register | POST   | `/api/auth/register` |
| Login    | POST   | `/api/auth/login`    |

---

### 📚 Book APIs

| Action      | Method                               | Role  |
| ----------- | ------------------------------------ | ----- |
| Add Book    | POST `/api/admin/books/add`          | ADMIN |
| Update Book | PUT `/api/admin/books/{id}`          | ADMIN |
| Delete Book | DELETE `/api/admin/books/delete{id}` | ADMIN |
| View Books  | GET `api/user/books`                 | USER  |
| View Books  | GET `api/admin/books`                | ADMIN |

---

### 🔄 Borrow / Return APIs

| Action      | Method                                |
| ----------- | ------------------------------------- |
| Borrow Book | POST `/api/user/borrow/book/{bookId}` |
| Return Book | POST `/api/user/borrow/return`        |

> ⚠️ Only one active borrow allowed per user.

---

## 📊 Monthly Report Scheduler

- Default cron: `0 59 23 L * ?` (last day of month at 23:59)
- Config key: `library.report.cron` in `application.yaml` for local overrides

Reports are logged to the console for review.

---

## 🧪 Testing with curl (Example)

```bash
curl -X POST http://localhost:8080/api/user/borrow/return \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

---

## 🧯 Error Handling

- Centralized exception handling
- Consistent API response format:

```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

---

## ✅ Coding Practices Followed

- MVC architecture
- Loose coupling & modular design
- DTO-based responses
- Custom exceptions
- Secure password storage
- Clean JPQL queries
- Role-based authorization

---

## 👨‍💻 Notes for Reviewers

- Scheduler logic can be extended to:
  - Export CSV / PDF
  - Store reports in DB

- JWT & security flow is production-aligned
- Designed for scalability & clarity over shortcuts

---

## 🏁 Conclusion

This project demonstrates:

- Real-world Spring Boot design
- Secure authentication
- Clean business logic separation
- Practical scheduling & reporting

Perfectly suitable for **technical evaluation and code review**.

---

Happy reviewing! 🚀
