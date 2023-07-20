
[![Maven Package upon a push](https://github.com/mosip/resident-services/actions/workflows/push_trigger.yml/badge.svg?branch=release-1.2.0)](https://github.com/mosip/resident-services/actions/workflows/push_trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_resident-services&id=mosip_resident-services&branch=release-1.2.0&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_resident-services&branch=release-1.2.0)

# Resident Services
This repository contains the source code and design documents for MOSIP Resident Service. For an overview please refer [here](https://docs.mosip.io/1.2.0/modules/resident-services). This module exposes API endpoints for Resident UI (refer [Resident UI github repo](https://github.com/mosip/resident-ui/blob/master/README.md)).


## Database
See [DB guide](db_scripts/README.md)

## Config-Server
To run Resident services, run [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)

## Build & run (for developers)
Prerequisites:

1. [Config Server](https://docs.mosip.io/1.2.0/modules/module-configuration#config-server)
1. JDK 1.11  
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
1. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```
## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Configuration
Refer to the [configuration guide](docs/configuration.md).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).


