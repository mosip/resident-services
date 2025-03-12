#!/bin/bash
# Installs resident service
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=resident
CHART_VERSION=1.3.0-beta.1
COPY_UTIL=../copy_cm_func.sh

echo Create $NS namespace
kubectl create ns $NS

function installing_resident() {
  echo Istio label
  kubectl label ns $NS istio-injection=enabled --overwrite
  helm repo update

  echo Copy configmaps
  $COPY_UTIL configmap global default $NS
  $COPY_UTIL configmap artifactory-share artifactory $NS
  $COPY_UTIL configmap config-server-share config-server $NS

  echo Copy secrets
  $COPY_UTIL secret keycloak-client-secrets keycloak $NS

  echo Setting up dummy values for Resident OIDC Client ID
  kubectl create secret generic resident-oidc-onboarder-key -n $NS --from-literal=resident-oidc-clientid='' --dry-run=client -o yaml | kubectl apply -f -
  $COPY_UTIL secret resident-oidc-onboarder-key resident config-server

  kubectl -n config-server set env --keys=resident-oidc-clientid --from secret/resident-oidc-onboarder-key deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server get deploy -o name | xargs -n1 -t kubectl -n config-server rollout status
  echo "Do you have public domain & valid SSL? (Y/n) "
  echo "Y: if you have public domain & valid ssl certificate"
  echo "n: If you don't have a public domain and a valid SSL certificate. Note: It is recommended to use this option only in development environments."
  read -p "" flag

  if [ -z "$flag" ]; then
    echo "'flag' was provided; EXITING;"
    exit 1;
  fi
  ENABLE_INSECURE=''
  if [ "$flag" = "n" ]; then
    ENABLE_INSECURE='--set enable_insecure=true';
  fi

  API_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-api-internal-host})
  RESIDENT_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-resident-host})

  echo Installing Resident
  helm -n $NS install resident mosip/resident --set istio.corsPolicy.allowOrigins\[0\].prefix=https://$RESIDENT_HOST --version $CHART_VERSION $ENABLE_INSECURE

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed Resident services
  echo Installed Resident UI

  echo "resident-ui portal URL: https://$RESIDENT_HOST/"
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_resident   # calling function
