-- Below script is required to rollback from 1.3.0 to 1.3.0-beta.1

\c mosip_master

DROP INDEX IF EXISTS resident.idx_resident_transaction_aid_crdtime_desc;