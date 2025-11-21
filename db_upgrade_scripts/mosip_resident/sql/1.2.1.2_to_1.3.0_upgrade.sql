-- Below script is required to upgrade from 1.3.0-beta.1 to 1.3.0

\c mosip_resident

CREATE INDEX idx_resident_transaction_aid_crdtime_desc
    ON resident.resident_transaction (aid, cr_dtimes DESC);