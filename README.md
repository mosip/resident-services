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


## Services

The Resident module contains the following services:

1. **[Resident Service](https://github.com/mosip/resident-services/tree/master/resident/resident-service)** - Core Resident service functionality


## Database

Before starting the local setup, execute the required SQL scripts to initialize the database.

All database SQL scripts are available in the [db_scripts](db_scripts) directory.

## Local Setup

The project can be set up in two ways:

1. [Local Setup (for Development or Contribution)](#local-setup-for-development-or-contribution)
2. [Local Setup with Docker (Easy Setup for Demos)](#local-setup-with-docker-easy-setup-for-demos)


### Prerequisites

Install or configure the following:

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **Docker**: Latest stable version
- **PostgreSQL**: 10.2
- **Keycloak**: [Check here](https://github.com/mosip/keycloak)

### Configuration

- Resident module uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
  Please refer to the required released tagged version for configuration.
  [Configuration-Resident](https://github.com/mosip/mosip-config/blob/master/resident-default.properties) and
  [Configuration-Application](https://github.com/mosip/mosip-config/blob/master/application-default.properties) are defined here. You need to run the config-server along with the files mentioned above.
- For generating clients, refer to MOSIP’s documentation here: [Client Generation Guide](https://docs.mosip.io/1.2.0/interoperability/integrations/mosip-crvs/approach/technical-details#id-1.-create-client-id-role-for-the-crvs)
- To authenticate a client, use the Auth Manager API as described here: [Auth API Documentation](https://docs.mosip.io/1.2.0/interoperability/integrations/mosip-crvs/approach/technical-details#id-2.-fetch-access-token-to-call-the-apis)

#### Required Configuration Properties

The following properties must be configured with your environment-specific values before deployment:

**Database Configuration:**
- `mosip.resident.database.hostname` - Database hostname (default: postgres-postgresql.postgres)
- `mosip.resident.database.port` - Database port (default: 5432)
- `db.dbuser.password` - Database user password (passed as environment variable)

**IAM/Keycloak Configuration:**
- `keycloak.internal.url` - Internal Keycloak URL (passed as environment variable)
- `keycloak.external.url` - External Keycloak URL (passed as environment variable)
- `mosip.resident.client.secret` - Resident client secret for Keycloak (passed as environment variable)

**Service URLs:**
- `mosip.kernel.authmanager.url` - Auth manager service URL
- `mosip.kernel.keymanager.url` - Key manager service URL
- `mosip.kernel.masterdata.url` - Masterdata service URL
- `mosip.kernel.notification.url` - Notification service URL
- `mosip.regproc.status.service.url` - Registration processor status service URL
- `mosip.regproc.transaction.service.url` - Registration processor transaction service URL
- `mosip.packet.receiver.url` - Packet receiver service URL
- `mosip.idrepo.identity.url` - ID repository identity service URL
- `mosip.ida.internal.url` - IDA internal service URL
- `mosip.idrepo.credrequest.generator.url` - Credential request generator service URL
- `mosip.idrepo.credential.service.url` - Credential service URL
- `mosip.idrepo.vid.url` - ID repository VID service URL
- `mosip.pms.partnermanager.url` - Partner manager service URL
- `mosip.resident.url` - Resident service URL
- `mosip.kernel.syncdata.url` - Sync data service URL
- `mosip.digitalcard.service.url` - Digital card service URL
- `mosip.kernel.ridgenerator.url` - RID generator service URL
- `mosip.kernel.otpmanager.url` - OTP manager service URL
- `mosip.kernel.auditmanager.url` - Audit manager service URL
- `mosip.api.internal.url` - Internal API base URL
- `mosip.api.public.url` - Public API base URL

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
