# MOSIP Resident Services

[![Maven Package upon a push](https://github.com/mosip/resident-services/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/resident-services/actions/workflows/push-trigger.yml)  
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_resident-services&id=mosip_resident-services&branch=release-1.3.x&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_resident-services&branch=release-1.3.0)

## Overview

Resident Services offer a set of self-service capabilities accessible through the online Resident Portal. This portal allows residents to manage and interact with their Unique Identification Number (UIN) and Virtual ID (VID). Through the platform, residents can perform identity-related operations, access credentials, and raise service requests or concerns.

It exposes a set of APIs consumed by **[Resident UI](https://github.com/mosip/resident-ui/)**

## Services

The Resident module contains the following services:

- **[Resident Service](https://github.com/mosip/resident-services/tree/master/resident/resident-service)** - Core Resident service.


## Database

Before starting the local setup, execute the required SQL scripts to initialize the database.

All database SQL scripts are available in the [db scripts](db_scripts) directory.

## Local Setup

The project can be set up in two ways:

1. [Local Setup (for Development or Contribution)](#local-setup-for-development-or-contribution)
2. [Local Setup with Docker (Easy Setup for Demos)](#local-setup-with-docker-easy-setup-for-demos)


### Prerequisites

Install or configure the following:

- **JDK**: 21.0.3
- **Maven**: 3.9.6
- **Docker**: Latest stable version
- **PostgreSQL**: 16.0
- **Keycloak**: [Check here](https://github.com/mosip/keycloak/tree/master)

### Configuration

- Resident module uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
  Please refer to the required released tagged version for configuration.
  - [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties) : Contains common configurations which are required across MOSIP modules. 
  - [resident-default.properties](https://github.com/mosip/mosip-config/blob/master/resident-default.properties) : Contains configurations required or to be overridden for resident module.

#### Required Configuration Properties

The following properties must be configured with your environment-specific values before deployment:

**Database Configuration:**
- mosip.resident.database.hostname - Database hostname (default: postgres-postgresql.postgres)
- mosip.resident.database.port - Database port (default: 5432)
- db.dbuser.password - Database user password (passed as environment variable)

**IAM/Keycloak Configuration:**
- keycloak.internal.url - Internal Keycloak URL (passed as environment variable)
- keycloak.external.url - External Keycloak URL (passed as environment variable)
- mosip.resident.client.secret - Resident client secret for Keycloak (passed as environment variable)
- mosip.keycloak.issuerUrl - Keycloak issuer URL

**Service URLs:**
- mosip.kernel.authmanager.url - Auth manager service URL
- mosip.kernel.keymanager.url - Key manager service URL
- mosip.kernel.masterdata.url - Masterdata service URL
- mosip.kernel.notification.url - Notification service URL
- mosip.regproc.status.service.url - Registration processor status service URL
- mosip.regproc.transaction.service.url - Registration processor transaction service URL
- mosip.packet.receiver.url - Packet receiver service URL
- mosip.idrepo.identity.url - ID repository identity service URL
- mosip.ida.internal.url - IDA internal service URL
- mosip.idrepo.credrequest.generator.url - Credential request generator service URL
- mosip.idrepo.credential.service.url - Credential service URL
- mosip.idrepo.vid.url - ID repository VID service URL
- mosip.pms.partnermanager.url - Partner manager service URL
- mosip.resident.url - Resident service URL
- mosip.kernel.syncdata.url - Sync data service URL
- mosip.digitalcard.service.url - Digital card service URL
- mosip.kernel.ridgenerator.url - RID generator service URL
- mosip.kernel.otpmanager.url - OTP manager service URL
- mosip.kernel.auditmanager.url - Audit manager service URL
- mosip.api.internal.url - Internal API base URL
- mosip.api.public.url - Public API base URL

**Security Configuration:**
- mosip.security.csrf-enable - Enable CSRF protection (default: false)
- mosip.security.secure-cookie - Enable secure cookies (default: false)

## Installation

### Local Setup (for Development or Contribution)

1. Make sure the config server is running. For detailed instructions on setting up and running the Resident server, refer to the [Resident Server Setup Guide](https://docs.mosip.io/1.2.0/id-lifecycle-management/identity-management/resident-services/develop/resident-services-developer-guide).

**Note**: Refer to the MOSIP Config Server Setup Guide for setup, and ensure the properties mentioned above in the configuration section are taken care of. Replace the properties with your own configurations (e.g., DB credentials, IAM credentials, URL).

2. Clone the repository:


```text
git clone <repo-url>
cd resident-service
```
3. Build the project:


```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

4. Start the application:
    - Click the Run button in your IDE, or
    - Run via command: 
   ```text
   java -jar target/resident-service:<$version>.jar
   ``` 
5. Verify Swagger is accessible at: http://localhost:8099/resident/v1/swagger-ui/index.html

### Local Setup with Docker (Easy Setup for Demos)

#### Option 1: Pull from Docker Hub

Recommended for users who want a quick, ready-to-use setup — testers, students, and external users.

Pull the latest pre-built images from Docker Hub using the following commands:

```text
docker pull mosipid/resident-service:1.3.0
```

#### Option 2: Build Docker Images Locally

Recommended for contributors or developers who want to modify or build the services from source.

1. Clone and build the project:

```text
git clone <repo-url>
cd resident-service
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```
2. Navigate to each service directory and build the Docker image:

```text
cd resident/<service-directory>
docker build -t <service-name> .
```

#### Running the Services

Start each service using Docker:

```text
docker run -d -p <port>:<port> --name <service-name> <service-name>
```

#### Verify Installation

Check that all containers are running:

```text
docker ps
```

Access the services at http://localhost:<port> using the port mappings listed above.

## Deployment

### Kubernetes

To deploy Resident services on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Usage

### Resident UI

For the complete Resident UI implementation and usage instructions, refer to the [Resident UI](https://github.com/mosip/resident-ui/).

## Documentation

### API Documentation:

API endpoints and mock server details are available via Stoplight
and Swagger documentation: API documentation is available [here](https://mosip.stoplight.io/docs/resident/bb7qdoshx4zlt-registration-process-workflow-callback-api)

### Product Documentation

To learn more about resident service from a functional perspective and use case scenarios, refer to our main documentation: [Click here](https://docs.mosip.io/1.2.0/modules/resident-services).

## Testing

Automated functional tests are available in the [Functional tests](api-test).

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/resident-services/issues)

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).