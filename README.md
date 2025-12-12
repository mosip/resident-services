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

## Database

Before starting the local setup, execute the required SQL scripts available in the **[db_scripts](db_scripts)** directory.

---

## Local Setup (for Development or Contribution)

### Prerequisites

Install or configure the following:

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **PostgreSQL**
- **Docker** (optional for containerized builds)
- **MOSIP Config Server**  
  (Guide: https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)

### Build the Project

```bash
cd resident-service
mvn install -Dgpg.skip=true
```

### Run the Application

Run from IDE or use:

```bash
java -jar target/resident-service-<version>.jar
```

Swagger URL depends on the `bootstrap.properties` context path.

---

## Required Configuration Updates

### Hibernate Dialect

Use version-neutral dialect for future PostgreSQL compatibility:

```
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Spring Boot 3.x Path Matching

```
spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER
```

### Add the Below Config to resident-default.properties

```
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

## Keymanager service
mosip.kernel.keymanager.hsm.keystore-type=OFFLINE
mosip.kernel.keymanager.hsm.config-path=/config/softhsm-application.conf
mosip.kernel.keymanager.hsm.keystore-pass=${softhsm.kernel.security.pin}

# Spring Boot 3.x change for actuator
management.endpoint.env.show-values=ALWAYS  

resident.template.purpose.success.AUTHENTICATION_REQUEST=mosip.event.type.AUTHENTICATION_REQUEST
resident.template.purpose.failure.AUTHENTICATION_REQUEST=mosip.event.type.AUTHENTICATION_REQUEST
```

### Update Exclusion List (No Auth Required)

```
mosip.service.end-points=/**/req/otp,/**/proxy/**,/**/validate-otp,/**/channel/verification-status/**,
 /**/req/credential/**,/**/req/card/*,/**/req/auth-history,/**/rid/check-status,/**/req/auth-lock,
 /**/req/auth-unlock,/**/req/update-uin,/**/req/print-uin,/**/req/euin,/**/credential/types,
 /**/req/policy/**,/**/aid/status,/**/individualId/otp,/**/mock/**,/**/callback/**,
 /**/download-card,/**/download/registration-centers-list/**,/**/download/supporting-documents/**,
 /**/vid/policy,/**/vid,/vid/**,/**/download/nearestRegistrationcenters/**,
 /**/authorize/admin/validateToken,/**/logout/user,/**/aid-stage/**
```

---

## Configuration Files

These files define environment-level configurations:

- **[resident-default.properties](https://github.com/mosip/mosip-config/blob/master/resident-default.properties)**
- **[application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)**

Ensure these are served through config-server for local and deployment environments.

---

## Default Context, Path & Port

See:

```
resident/resident-service/src/main/resources/bootstrap.properties
```

---

## Local Setup with Docker (Easy Setup for Demos)

### Build Docker Image

```bash
cd <service-folder>
docker build -f Dockerfile -t mosip/resident-service .
```

### Run the Container

```bash
docker run -d -p <port>:<port> --name resident-service mosip/resident-service
```

Check running containers:

```bash
docker ps
```

---

## Deployment (Kubernetes)

### Prerequisites

Set your kubeconfig:

```bash
export KUBECONFIG=~/.kube/<k8s-cluster.config>
```

### Install Resident Services

```bash
cd deploy
./install.sh
```

### Delete Deployment

```bash
./delete.sh
```

### Restart Deployment

```bash
./restart.sh
```

---

## API Documentation

API reference and mock server details are available here:  
 **https://mosip.github.io/documentation/**

---

## Testing

Automated functional tests are available in the **[API Test repository](api-test)**.

---

## Contribution & Community

• Contribution guide:  
https://docs.mosip.io/1.2.0/community/code-contributions

• Community discussions:  
https://community.mosip.io/

• Report issues:  
https://github.com/mosip/resident-services/issues

---

## License

This project is released under the **Mozilla Public License 2.0**.  
See the [LICENSE](LICENSE) file.
