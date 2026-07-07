package br.com.fiapx.processor.infrastructure.persistence;

import br.com.fiapx.processor.application.port.output.ProcessingJobRepositoryPort;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProcessingJobRepositoryAdapter implements ProcessingJobRepositoryPort {

    private final ProcessingJobJpaRepository jpaRepository;

    public ProcessingJobRepositoryAdapter(ProcessingJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProcessingJob save(ProcessingJob job) {
        return jpaRepository.save(ProcessingJobEntity.fromDomain(job)).toDomain();
    }

    @Override
    public Optional<ProcessingJob> findById(UUID id) {
        return jpaRepository.findById(id).map(ProcessingJobEntity::toDomain);
    }

    @Override
    public Optional<ProcessingJob> findByUploadId(UUID uploadId) {
        return jpaRepository.findByUploadId(uploadId).map(ProcessingJobEntity::toDomain);
    }

    @Override
    public List<ProcessingJob> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(ProcessingJobEntity::toDomain).toList();
    }
}
