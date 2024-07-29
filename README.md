
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
1. JDK 1.21  
1. Build and install:
    ```
    $ cd resident-services
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```

## Add Below Config in [resident-default.properties](https://github.com/mosip/mosip-config/blob/develop/resident-default.properties)
```
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
## Keymanager service
#Type of keystore, Supported Types: PKCS11, PKCS12, Offline, JCE
mosip.kernel.keymanager.hsm.keystore-type=OFFLINE
# For PKCS11 provide Path of config file.
# For PKCS12 keystore type provide the p12/pfx file path. P12 file will be created internally so provide only file path & file name.
# For Offline & JCE property can be left blank, specified value will be ignored.
mosip.kernel.keymanager.hsm.config-path=/config/softhsm-application.conf
# Passkey of keystore for PKCS11, PKCS12
# For Offline & JCE proer can be left blank. JCE password use other JCE specific properties.

mosip.kernel.keymanager.hsm.keystore-pass=${softhsm.kernel.security.pin}

# For Spring-boot 3.x we need to specify the ANT Path Matcher for using the existing ANT path patterns.
spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER

# Spring boot 3.x onwards we need to specify the below property to unmask values in actuator env url
management.endpoint.env.show-values=ALWAYS  

resident.template.purpose.success.AUTHENTICATION_REQUEST=mosip.event.type.AUTHENTICATION_REQUEST
resident.template.purpose.failure.AUTHENTICATION_REQUEST=mosip.event.type.AUTHENTICATION_REQUEST
```

## Update below config in [resident-default.properties](https://github.com/mosip/mosip-config/blob/develop/resident-default.properties)
# The exclusion list of URL patterns that should not be part of authentication and authorization
mosip.service.end-points=/**/req/otp,/**/proxy/**,/**/validate-otp,/**/channel/verification-status/**,/**/req/credential/**,/**/req/card/*,/**/req/auth-history,/**/rid/check-status,/**/req/auth-lock,/**/req/auth-unlock,/**/req/update-uin,/**/req/print-uin,/**/req/euin,/**/credential/types,/**/req/policy/**,/**/aid/status,/**/individualId/otp,/**/mock/**,/**/callback/**,/**/download-card,/**/download/registration-centers-list/**,/**/download/supporting-documents/**,/**/vid/policy,/**/vid,/vid/**,/**/download/nearestRegistrationcenters/**,/**/authorize/admin/validateToken,/**/logout/user,/**/aid-stage/**

1. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```
## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Configuration
[Configuration](https://github.com/mosip/mosip-config/blob/develop/resident-default.properties) defined here.

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

