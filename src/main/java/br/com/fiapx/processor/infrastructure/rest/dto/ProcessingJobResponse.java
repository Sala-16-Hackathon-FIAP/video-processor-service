package br.com.fiapx.processor.infrastructure.rest.dto;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingJobResponse(
    UUID id,
    UUID uploadId,
    UUID userId,
    String originalFilename,
    ProcessingStatus status,
    String resultS3Key,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProcessingJobResponse fromDomain(ProcessingJob j) {
        return new ProcessingJobResponse(j.id(), j.uploadId(), j.userId(), j.originalFilename(),
                j.status(), j.resultS3Key(), j.errorMessage(), j.createdAt(), j.updatedAt());
    }
}
