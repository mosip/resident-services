CREATE INDEX idx_resident_transaction_aid_crdtime_desc
    ON resident.resident_transaction (aid, cr_dtimes DESC);
    
    drop index idx_resident_transaction_aid_crdtime_desc if exists;