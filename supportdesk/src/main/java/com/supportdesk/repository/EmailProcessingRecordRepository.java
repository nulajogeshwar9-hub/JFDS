package com.supportdesk.repository;

import com.supportdesk.entity.EmailProcessingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailProcessingRecordRepository extends JpaRepository<EmailProcessingRecord, Long> {
    Optional<EmailProcessingRecord> findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
}
