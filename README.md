# mini-rib

Mini Retail Internet Banking - training project. Currently has the Login API.

Spring Boot 3.5, Java 21, MySQL 8, Spring Security, JWT.

## Setup

Database:

```sql
CREATE DATABASE mini_rib CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mini_rib'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON mini_rib.* TO 'mini_rib'@'localhost';
```

Environment variables:

- `DB_PASSWORD` - MySQL password
- `JWT_SECRET` - at least 32 characters

Run:

```
mvn spring-boot:run
```

Swagger: http://localhost:8080/mini-rib/swagger-ui.html

## API

`POST /api/v1/auth/login`

```json
{ "loginId": "karim.ahmed", "password": "Str0ng@Pass" }
```

Test users `karim.ahmed` and `nusrat.jahan`, password `Str0ng@Pass`.
