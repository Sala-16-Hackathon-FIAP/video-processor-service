package br.com.fiapx.processor.application.port.output;

import br.com.fiapx.processor.domain.model.ProcessingJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingJobRepositoryPort {
    ProcessingJob save(ProcessingJob job);
    Optional<ProcessingJob> findById(UUID id);
    Optional<ProcessingJob> findByUploadId(UUID uploadId);
    List<ProcessingJob> findByUserId(UUID userId);
}
