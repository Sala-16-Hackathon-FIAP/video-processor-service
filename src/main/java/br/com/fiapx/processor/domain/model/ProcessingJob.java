package br.com.fiapx.processor.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingJob(
    UUID id,
    UUID uploadId,
    UUID userId,
    String originalFilename,
    String sourceS3Key,
    String resultS3Key,
    ProcessingStatus status,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProcessingJob create(UUID uploadId, UUID userId, String filename, String sourceKey) {
        return new ProcessingJob(UUID.randomUUID(), uploadId, userId, filename, sourceKey, null,
                ProcessingStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public ProcessingJob started() {
        return new ProcessingJob(id, uploadId, userId, originalFilename, sourceS3Key, resultS3Key,
                ProcessingStatus.PROCESSING, null, createdAt, LocalDateTime.now());
    }

    public ProcessingJob completed(String resultKey) {
        return new ProcessingJob(id, uploadId, userId, originalFilename, sourceS3Key, resultKey,
                ProcessingStatus.COMPLETED, null, createdAt, LocalDateTime.now());
    }

    public ProcessingJob failed(String reason) {
        return new ProcessingJob(id, uploadId, userId, originalFilename, sourceS3Key, resultS3Key,
                ProcessingStatus.FAILED, reason, createdAt, LocalDateTime.now());
    }
}
