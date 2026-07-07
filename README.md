# Support Management System API

REST API สำหรับระบบจัดการ Support Ticket สร้างด้วย Spring Boot 4.x และ PostgreSQL

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0** (Security, JPA, Validation, Web MVC)
- **PostgreSQL 17**
- **JWT** (jjwt 0.12.6) — Access + Refresh Token
- **Bucket4j** — Rate limiting
- **SpringDoc OpenAPI** — Swagger UI
- **Gradle** — Build tool

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Gradle | 9.5.1 (wrapper included) |
| PostgreSQL | 17 (หรือใช้ Docker) |
| Docker (optional) | ใช้สำหรับ run PostgreSQL |

---

## Quick Start

### 1. Start PostgreSQL

**ด้วย Docker (แนะนำ):**

```bash
docker compose up -d
```

**หรือ PostgreSQL ที่ติดตั้งเอง:**

สร้าง database:
```sql
CREATE DATABASE support_management_db;
```

---

### 2. Run Application (Local Profile)

```bash
./gradlew bootRun
```

`bootRun` จะ activate profile `local` โดยอัตโนมัติ และใช้ค่าจาก `application-local.yaml`

API พร้อมใช้งานที่: `http://localhost:8080`

---

### 3. Default Admin Account

เมื่อ startup ครั้งแรก ระบบ seed admin user อัตโนมัติ (เมื่อไม่มี user ในฐานข้อมูล):

| Field | Value |
|-------|-------|
| Email | `admin@example.com` |
| Password | `Admin@1234` |

---

## Environment Variables

### Production / Non-local

ต้องตั้งค่า env ต่อไปนี้:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC URL ของ PostgreSQL | `jdbc:postgresql://localhost:5432/support_management_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `your_password` |
| `JWT_SECRET` | JWT signing key (Base64, 256-bit+) | `PBZOB6wU8DpArV73Nrbwav++3kWkTTJsX4zfYczrraE=` |

**Optional:**

| Variable | Description | Default |
|----------|-------------|---------|
| `JPA_DDL_AUTO` | Hibernate DDL strategy | `validate` |

### Local Profile

`application-local.yaml` ตั้งค่าไว้แล้ว ไม่ต้องตั้ง env เพิ่ม:

```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/support_management_db
  username: postgres
  password: postgres

jwt:
  secret: PBZOB6wU8DpArV73Nrbwav++3kWkTTJsX4zfYczrraE=  # fallback ถ้าไม่มี JWT_SECRET
```

---

## Configuration Reference

### JWT (`application.security.jwt`)

| Property | Default | Description |
|----------|---------|-------------|
| `secret` | — | JWT signing key |
| `access-token-expiration` | `3600000` ms (1 ชั่วโมง) | อายุ Access Token |
| `refresh-token-expiration` | `2592000000` ms (30 วัน) | อายุ Refresh Token |

### File Upload (`app.upload`)

| Property | Default | Description |
|----------|---------|-------------|
| `max-file-size-bytes` | `10485760` (10 MB) | ขนาดไฟล์สูงสุดต่อไฟล์ |
| `allowed-content-types` | JPEG, PNG, PDF, DOC, DOCX | MIME types ที่อนุญาต |
| `local-base-path` | `~/uploads` | path สำหรับเก็บไฟล์ |

> Spring multipart limit แยกต่างหาก: `max-file-size: 50MB`, `max-request-size: 50MB`

### Admin Seed (`application.init`)

| Property | Default | Description |
|----------|---------|-------------|
| `admin-email` | `admin@example.com` | Email admin เริ่มต้น |
| `admin-password` | `Admin@1234` | Password admin เริ่มต้น |

---

## API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Project Structure

```
src/main/java/com/ticket/support_management_system_api/
├── common/                  # Shared utilities, exceptions, responses
│   ├── entity/              # BaseEntity (audit fields)
│   ├── enums/               # CommonStatus, AccountType
│   ├── exception/           # GlobalExceptionHandler, custom exceptions
│   └── response/            # ApiResponse, PageResponse wrappers
├── config/                  # Spring configuration beans
├── features/
│   ├── auth/                # Login, logout, JWT, refresh token, device session
│   ├── company/             # Company management
│   ├── department/          # Department management
│   ├── priority/            # Priority level management
│   ├── project/             # Project + member management, document upload
│   ├── status/              # Status & status flow management
│   ├── ticket_category/     # Ticket category
│   ├── ticket_sub_category/ # Ticket sub-category
│   ├── ticket_type/         # Ticket type
│   ├── upload/              # File upload endpoint
│   ├── user/                # User management
│   └── user_type/           # User type & permission management
└── infrastructure/
    ├── email/               # Email service
    └── storage/             # File storage service
```

---

## Build

```bash
# Build JAR
./gradlew build

# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

---

## Auto-Assign & Rebalance Suggestion

### 1. Auto-assign ตอนสร้าง Ticket

เมื่อสร้าง Ticket ใหม่ (`POST /api/v1/tickets`) ระบบจะเลือกผู้รับผิดชอบให้อัตโนมัติ โดยไม่ต้องให้ผู้ใช้เลือกเอง:

1. หา `ProjectMember` ที่มี role `ASSIGNEE` ในโครงการเดียวกับ Ticket
2. กรองเฉพาะคนที่ตำแหน่ง (`ExternalDetails.position`) ตรงกับตำแหน่งของ Sub Category
3. ในกลุ่มที่ตำแหน่งตรงกัน เลือกคนที่มี Ticket ที่ยังไม่ปิด (`currentStatus.group` ไม่ใช่ `SUCCESS`/`FAILED`) **น้อยที่สุด**
4. บันทึกเป็น `TicketAssignee` + log ใน `TicketAssigneeLog` (action `ADDED`) + แจ้งเตือน `TICKET_ASSIGNED`

ถ้าไม่มีสมาชิกในโครงการ หรือไม่มีใครตำแหน่งตรงกัน ระบบจะไม่ auto-assign ให้ (Ticket จะไม่มีผู้รับผิดชอบ ต้องมอบหมายเอง) — logic นี้ยังใช้ร่วมกับ endpoint เดิม `GET /tickets/{id}/suggested-assignee`

### 2. Rebalance Suggestion (แนะนำโอนงาน)

เมื่อ Ticket ใกล้ครบกำหนด (เหลือเวลา ≤ 30 นาที) และยังไม่ปิด ระบบจะเช็คว่าผู้รับผิดชอบปัจจุบัน **assign เกินภาระ** หรือไม่ เทียบกับค่าเฉลี่ยของเพื่อนร่วมตำแหน่งเดียวกันในโครงการเดียวกัน (`openCount(user) > average(openCount ของสมาชิก ASSIGNEE ตำแหน่งเดียวกัน)`) ถ้าเกิน จะแนะนำให้โอนงานไปยังคนที่มี Ticket ค้างน้อยที่สุดในกลุ่มเดียวกัน

- **Background job** (`RebalanceSuggestionService.notifyOverloadedAssigneesForDueSoonTickets`, ทุก 1 นาที): สแกน Ticket ที่ใกล้ครบกำหนดและยังไม่เคยแจ้ง (`Ticket.rebalanceSuggestedAt IS NULL`) ประเมินแล้วส่ง notification ประเภท `TICKET_REBALANCE_SUGGESTED` ให้เฉพาะผู้รับผิดชอบที่เกินภาระ (ไม่ broadcast ทั้ง ticket) จากนั้น mark `rebalanceSuggestedAt` กันแจ้งซ้ำ
- **On-demand endpoint** `GET /api/v1/tickets/{id}/rebalance-suggestion`: ให้หน้า Ticket Detail เรียกดูสดๆ เพื่อแสดง suggest banner (คำนวณสดทุกครั้ง ไม่ผูกกับ flag)
- `rebalanceSuggestedAt` จะถูก reset เป็น `null` ทุกครั้งที่มีการเพิ่ม/ลบผู้รับผิดชอบ Ticket เพื่อให้ระบบเช็คใหม่ได้ถ้าเกิด overload อีกครั้งหลังเปลี่ยนตัว

> **หมายเหตุ DB migration**: โปรเจกต์นี้ใช้ `ddl-auto: validate` (ไม่มี Flyway/Liquibase) เพิ่มคอลัมน์ `rebalance_suggested_at` ใน entity `Ticket` แล้ว ต้องรัน SQL นี้กับฐานข้อมูลจริงก่อน deploy:
> ```sql
> ALTER TABLE tickets ADD COLUMN rebalance_suggested_at TIMESTAMP;
> ```

---

## Production Example

```bash
export DB_URL=jdbc:postgresql://db-host:5432/support_management_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_256bit_base64_key
export JPA_DDL_AUTO=validate

java -jar build/libs/support-management-system-api-0.0.1-SNAPSHOT.jar
```

**Generate JWT Secret:**

```bash
openssl rand -base64 32
```
