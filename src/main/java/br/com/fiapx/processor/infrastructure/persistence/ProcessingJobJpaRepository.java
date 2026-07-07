package br.com.fiapx.processor.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingJobJpaRepository extends JpaRepository<ProcessingJobEntity, UUID> {
    Optional<ProcessingJobEntity> findByUploadId(UUID uploadId);
    List<ProcessingJobEntity> findByUserId(UUID userId);
}
