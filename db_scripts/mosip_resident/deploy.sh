## Properties file
set -e
properties_file="$1"
echo `date "+%m/%d/%Y %H:%M:%S"` ": $properties_file"
echo "Loaded SCHEMA_NAME: $SCHEMA_NAME"


if [ -f "$properties_file" ]; then
    echo "$(date "+%m/%d/%Y %H:%M:%S") : Property file \"$properties_file\" found."
    while IFS='=' read -r key value; do
        key=$(echo "$key" | tr '.' '_' | xargs)
        value=$(echo "$value" | xargs)
        eval "${key}='${value}'"
    done < <(grep -v '^\s*#' "$properties_file" | grep '=')
else
    echo "$(date "+%m/%d/%Y %H:%M:%S") : Property file not found, Pass property file name as argument."
    exit 1
fi

echo "Loaded SCHEMA_NAME: $SCHEMA_NAME"
## Terminate existing connections
echo "Terminating active connections" 
CONN=$(PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -t -c "SELECT count(pg_terminate_backend(pg_stat_activity.pid)) FROM pg_stat_activity WHERE datname = '$MOSIP_DB_NAME' AND pid <> pg_backend_pid();"; exit)
echo "Terminated connections"

## Drop DB
echo "Dropping DB"
PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
    -v mosip_db_name=$MOSIP_DB_NAME \
    --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -f drop_db.sql

## Drop user
echo "Dropping user"
PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
    -v resident_user_name=$RESIDENT_USER_NAME \
    --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -f drop_role.sql

## Create users
echo `date "+%m/%d/%Y %H:%M:%S"` ": Creating database users" 
PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
    -v resident_user_name=$RESIDENT_USER_NAME \
    -v dbuserpwd=\'$DBUSER_PWD\' \
    --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -f role_dbuser.sql

## Create DB
echo "Creating DB"
PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
    -v mosip_db_name=$MOSIP_DB_NAME \
    -v db_user=$DB_USER \
    --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -f db.sql

PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
    -v mosip_db_name=$MOSIP_DB_NAME \
    --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
    --dbname=$DEFAULT_DB_NAME -f ddl.sql

PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
  -v mosip_db_name=$MOSIP_DB_NAME \
  -v resident_user_name=$RESIDENT_USER_NAME \
  -v schema_name=$SCHEMA_NAME \
  --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
  --dbname=$DEFAULT_DB_NAME -f grants.sql


## Populate tables
if [ "$DML_FLAG" == "1" ]; then
    echo `date "+%m/%d/%Y %H:%M:%S"` ": Deploying DML for ${MOSIP_DB_NAME} database" 
    PGPASSWORD=$SU_USER_PWD psql --set=ON_ERROR_STOP=1 \
        --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT \
        --dbname=$DEFAULT_DB_NAME -a -b -f dml.sql 
fi
