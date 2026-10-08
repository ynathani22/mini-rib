# mini-rib

Mini Retail Internet Banking - training project.

Spring Boot 3.5, Java 21, MySQL 8, Spring Security, JWT, BCrypt, Apache Camel, JasperReports.

## Features

- Register (CIF is checked against core banking, password stored as BCrypt hash)
- Login (BCrypt check, returns JWT, locks user after 3 wrong passwords)
- Account summary (compares core banking data with DB and syncs only the differences)
- Account summary PDF download (Jasper)
- Core banking stub: customer/account data is kept in JSON files under `stub-data/` and read through an Apache Camel route, so data can be changed without touching the code

## Setup

### 1. Database

```sql
CREATE DATABASE mini_rib CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mini_rib'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON mini_rib.* TO 'mini_rib'@'localhost';
FLUSH PRIVILEGES;
```

Tables (`app_user`, `fnd_corp_cif_account`) are created automatically from `src/main/resources/db/schema.sql` on startup.

### 2. Environment variables

| Variable | Value |
|---|---|
| `DB_PASSWORD` | MySQL password of `mini_rib` user |
| `JWT_SECRET` | any text, at least 32 characters |
| `STUB_DATA_DIR` | optional, full path to `stub-data` folder (default: `stub-data`) |

In IntelliJ: Run > Edit Configurations > MiniRibApplication > Environment variables

```
DB_PASSWORD=change_me;JWT_SECRET=my-very-long-random-secret-key-1234567890
```

### 3. Run

```
mvn spring-boot:run
```

or run `MiniRibApplication` from IntelliJ.

Base URL: `http://localhost:8080/mini-rib`

## Core banking data (stub-data)

Each customer has one JSON file named after the CIF, e.g. `stub-data/CIF0004.json`:

```json
[
  { "cif": "CIF0004", "accNumber": "1004000001", "accHolderName": "Yashika Nathani", "currCode": "BDT", "dtype": "CASA", "status": "ACTIVE" }
]
```

- `dtype`: CASA, LOAN, DEPOSIT, CREDIT, PREPAID
- `status`: ACTIVE, INACTIVE
- To add a customer, add a new file. To change an account, edit the file. No restart needed.

## Test users

| Login ID | Password | CIF |
|---|---|---|
| yashika.nathani | Hell0@world | CIF0001 |
| khyati.kalia | Hell0@world | CIF0004 |

On a fresh database these users do not exist yet, register them first (step 1 below).

## API

### 1. Register

`POST /api/v1/auth/register`

```json
{
  "cif": "CIF0001",
  "loginId": "yahika.nathani",
  "password": "Hell0@world",
  "confirmPassword": "Hell0@world",
  "email": "yashika@example.com",
  "mobileNo": "01711000002"
}
```

```json
{
  "cif": "CIF0004",
  "loginId": "khyati.kalia",
  "password": "Hell0@world",
  "confirmPassword": "Hell0@world",
  "email": "khyati@example.com",
  "mobileNo": "01711000002"
}
```

- 201 - registered, `fullName` is taken from core banking
- 404 `CIF_NOT_FOUND` - no file for this CIF in `stub-data`
- 409 `CIF_ALREADY_REGISTERED` / `LOGIN_ID_TAKEN`
- 400 `VALIDATION_FAILED` - weak password, passwords don't match, etc.

### 2. Login

`POST /api/v1/auth/login`

```json
{ "loginId": "yashika.nathani", "password": "Hell0@world" }
```

- 200 - returns `accessToken` (valid 15 minutes) and user details
- 401 `INVALID_CREDENTIALS` - wrong login ID or password
- 403 `USER_LOCKED` - after 3 wrong passwords

### 3. Account summary

`GET /api/v1/accounts/summary`

Header: `Authorization: Bearer <accessToken>`

CIF is taken from the token. Response shows `syncResult`:
- `UPDATED_FROM_CORE` - new or changed accounts were saved to DB (`inserted`, `updated` counts)
- `NO_CHANGE` - DB already matches core banking

### 4. Account summary PDF

`GET /api/v1/accounts/summary/pdf`

Header: `Authorization: Bearer <accessToken>`

In Postman use "Send and Download".

## Testing the sync

1. Login and call summary - first call inserts all accounts (`inserted` > 0)
2. Call again - `NO_CHANGE`
3. In the CIF's JSON file change an account `status` to `INACTIVE`, save, call summary - `updated: 1`, account version increases
4. Add a new account object to the same file, save, call summary - `inserted: 1`

## Swagger

http://localhost:8080/mini-rib/swagger-ui.html

## Tests

```
mvn test
```
