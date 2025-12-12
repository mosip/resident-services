# Resident Services

[![Maven Package upon a push](https://github.com/mosip/resident-services/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/resident-services/actions/workflows/push-trigger.yml)  
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_resident-services&id=mosip_resident-services&branch=release-1.3.x&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_resident-services&branch=release-1.3.0)

## Overview

Resident Services provide a suite of self-service tools that residents can access through the online Resident Portal.  
The portal enables residents to manage and interact with their Unique Identification Number (UIN) and Virtual ID (VID). Through this platform, residents can perform various identity-related operations, access credentials, and raise service requests or concerns.

It exposes a set of APIs consumed by **Resident UI**, available here:  
**[Resident UI GitHub Repository](https://github.com/mosip/resident-ui/)**

For a functional overview, refer to the official documentation:  
https://docs.mosip.io/1.2.0/modules/resident-services

---

## Services

The Resident module contains the following service:

1. **[Resident Service](https://github.com/mosip/resident-services/tree/master/resident/resident-service)** – Core Resident service functionality

---

## Database

Before starting the local setup, execute the required SQL scripts to initialize the database.  
All database SQL scripts are available in the **[db_scripts](db_scripts)** directory.

---

## Local Setup

The project can be set up in two ways:

1. [Local Setup (for Development or Contribution)](#local-setup-for-development-or-contribution)
2. [Local Setup with Docker (Easy Setup for Demos)](#local-setup-with-docker-easy-setup-for-demos)

### Prerequisites

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **Docker**: Latest stable version
- **PostgreSQL**: 10.2
- **Keycloak**: https://github.com/mosip/keycloak

### Configuration

Resident module uses configuration files from the **mosip-config** repository:

- **[resident-default.properties](https://github.com/mosip/mosip-config/blob/master/resident-default.properties)**
- **[application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)**

Ensure the MOSIP Config Server is running.

---

## Required Configuration Properties

**Database Configuration:**
- `mosip.resident.database.hostname` – Database hostname
- `mosip.resident.database.port` – Database port
- `db.dbuser.password` – Database user password (env variable)

**IAM/Keycloak Configuration:**
- `keycloak.internal.url`
- `keycloak.external.url`
- `mosip.resident.client.secret`
- `mosip.keycloak.issuerUrl`

**Service URLs:**  
(authmanager, keymanager, masterdata, notification, idrepo, ida, credential services, syncdata, auditmanager, otpmanager, pms, etc.)

**Security Configuration:**
- `mosip.security.csrf-enable`
- `mosip.security.secure-cookie`

**Note:** Environment variables MUST NOT be inside property files when using config-server.

---

## Installation

### Local Setup (for Development or Contribution)

1. Run Config Server
2. Clone the repository:

```bash
git clone <repo-url>
cd resident-services
```

3. Build the project:

```bash
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

4. Start the application:

```bash
java -jar resident/resident-service/target/resident-service-<version>.jar
```

5. Verify Swagger:
```
http://localhost:8099/resident/v1/swagger-ui/index.html
```

---

## Local Setup with Docker (Easy Setup for Demos)

### Option 1: Pull from Docker Hub

```bash
docker pull mosipid/resident-service:1.2.1.2
```

### Option 2: Build Docker Image Locally

```bash
cd resident-services
mvn clean install -Dgpg.skip=true
cd resident/resident-service
docker build -t resident-service .
```

### Run the Service

```bash
docker run -d -p 8099:8099 --name resident-service resident-service
```

---

## Deployment

### Kubernetes

For cluster deployment, refer to the **Sandbox Deployment Guide**:  
https://docs.mosip.io/1.2.0/deploymentnew/v3-installation

---

## Usage

### Resident UI

A complete UI implementation is available here:  
https://github.com/mosip/resident-ui/

---

## Documentation

- **Repository Docs:** https://github.com/mosip/documentation/tree/1.2.0/docs
- **API Documentation:** https://mosip.stoplight.io/docs/resident
- **Product Documentation:**  
  https://docs.mosip.io/1.2.0/id-lifecycle-management/identity-management/resident-services

---

## Testing

Automated functional tests are available in the **[Functional Tests repository](api-test)**.

---

## Contribution & Community

- Contribution Guide: https://docs.mosip.io/1.2.0/community/code-contributions
- Community Support: https://community.mosip.io/
- Report Issues: https://github.com/mosip/resident-services/issues

---

## License

This project is licensed under the **Mozilla Public License 2.0**.  
See the [LICENSE](LICENSE) file.
