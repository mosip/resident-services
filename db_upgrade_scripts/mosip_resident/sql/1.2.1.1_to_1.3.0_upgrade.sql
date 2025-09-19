CREATE INDEX idx_resident_transaction_aid_crdtime_desc
    ON resident.resident_transaction (aid, cr_dtimes DESC);