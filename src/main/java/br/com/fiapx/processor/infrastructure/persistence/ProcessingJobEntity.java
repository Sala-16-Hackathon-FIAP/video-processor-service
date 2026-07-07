package br.com.fiapx.processor.infrastructure.persistence;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_jobs")
public class ProcessingJobEntity {

    @Id
    private UUID id;

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "source_s3_key", nullable = false)
    private String sourceS3Key;

    @Column(name = "result_s3_key")
    private String resultS3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProcessingJobEntity() {}

    public static ProcessingJobEntity fromDomain(ProcessingJob j) {
        ProcessingJobEntity e = new ProcessingJobEntity();
        e.id = j.id();
        e.uploadId = j.uploadId();
        e.userId = j.userId();
        e.originalFilename = j.originalFilename();
        e.sourceS3Key = j.sourceS3Key();
        e.resultS3Key = j.resultS3Key();
        e.status = j.status();
        e.errorMessage = j.errorMessage();
        e.createdAt = j.createdAt();
        e.updatedAt = j.updatedAt();
        return e;
    }

    public ProcessingJob toDomain() {
        return new ProcessingJob(id, uploadId, userId, originalFilename, sourceS3Key,
                resultS3Key, status, errorMessage, createdAt, updatedAt);
    }
}
