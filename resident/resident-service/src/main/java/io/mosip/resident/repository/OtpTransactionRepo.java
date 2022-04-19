package io.mosip.resident.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.resident.entity.OtpTransaction;

public interface OtpTransactionRepo extends JpaRepository<OtpTransaction, String> {

}
