# Health Assistance System

A JavaFX desktop application for patient, doctor, and admin healthcare workflows, including appointment scheduling, health record management, and reminder notifications.

## Overview

Health Assistance System is a role-based healthcare management application built with JavaFX and MySQL.
It supports:
- secure login and role-based dashboards
- appointment booking and management
- patient and doctor profile workflows
- health record persistence
- background appointment reminders with UI alerts and sound

## Key Features

- Role-based authentication (`Admin`, `Doctor`, `Patient`)
- Appointment CRUD with conflict prevention
- Automatic expiration handling for past appointments
- Health records and prescription storage
- Background reminder service (`AppointmentReminderTask`) using JavaFX-safe UI updates
- Dashboard views tailored per user role
- Built-in test accounts for local validation

## Architecture

The project follows a layered desktop architecture:
- `application`: JavaFX app entry point
- `controller`: UI interaction and navigation logic
- `dao`: JDBC data access and SQL operations
- `database`: connection + schema scripts
- `model`: domain entities
- `resources`: FXML layouts, CSS, and images
- `util`: background services (reminder thread)

## Tech Stack

- Java
- JavaFX (FXML, CSS)
- JDBC
- MySQL
- Eclipse project configuration (`.classpath`, `.project`)

## Project Structure

```text
HealthAssistanceSystem/
|-- src/
|   |-- application/
|   |-- controller/
|   |-- dao/
|   |-- database/
|   |-- model/
|   |-- resources/
|   |   |-- css/
|   |   |-- fxml/
|   |   '-- images/
|   '-- util/
|-- bin/
|-- Documentation/
|-- resources/
'-- build.fxbuild
```

## Prerequisites

- JDK 17 or newer
- JavaFX SDK available to your IDE/runtime
- MySQL Server 8+
- (Recommended) Eclipse with JavaFX support

## Getting Started

### 1. Configure the Database

Create database and tables with the provided schema:

```sql
SOURCE src/database/schema.sql;
```

If `SOURCE` is not available in your shell:

```bash
mysql -u root -p < src/database/schema.sql
```

### 2. Configure DB Connection

Update connection settings in `src/database/DBConnection.java`:
- JDBC URL
- MySQL username
- MySQL password

Current default points to:
- host: `localhost`
- database: `health_assistance`

### 3. Run the Application

#### Option A: Eclipse (recommended)

1. Import as an existing Eclipse project.
2. Ensure JavaFX libraries are configured (project already references JavaFX containers in `.classpath`).
3. Run `application.Main`.

#### Option B: Command line (example)

```bash
javac --module-path "<PATH_TO_FX>" --add-modules javafx.controls,javafx.fxml,javafx.media -cp "<PATH_TO_MYSQL_CONNECTOR>" -d bin src/application/Main.java
java --module-path "<PATH_TO_FX>" --add-modules javafx.controls,javafx.fxml,javafx.media -cp "bin;<PATH_TO_MYSQL_CONNECTOR>" application.Main
```

Replace:
- `<PATH_TO_FX>` with JavaFX SDK `lib` path
- `<PATH_TO_MYSQL_CONNECTOR>` with MySQL Connector/J jar

## Test Accounts

Use these accounts on the login screen:

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Patient | patient | patient123 |
| Doctor | doctor | doctor123 |

## Online Shared Database (VPS)

To allow all users to share one database:

1. Host MySQL on your VPS.
2. Update `DBConnection.java` URL from `localhost` to your VPS IP/domain.
3. Open port `3306` only for trusted client IPs.
4. Create an application DB user with restricted permissions.
5. Import `schema.sql` once on the VPS DB.

## Security Notes

- Current authentication uses plain-text password comparison in the database.
- For production use, migrate to password hashing (for example, BCrypt).
- Do not keep production DB credentials hardcoded in source code.

## Documentation

Detailed project report is available in:
- `Documentation/Project_Documentation.tex`

## Contributors

- Kamdeu Yamdjeuson Neil Marshall (Scrum Master)
- Lemma Precious (Product Owner)

## Repository

- https://github.com/Kynmmarshall/HealthAssistanceSystem
