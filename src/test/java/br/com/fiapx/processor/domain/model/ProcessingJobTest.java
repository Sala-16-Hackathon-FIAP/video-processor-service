package br.com.fiapx.processor.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingJobTest {

    @Test
    void create_shouldInitializeWithPendingStatus() {
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProcessingJob job = ProcessingJob.create(uploadId, userId, "video.mp4", "uploads/key");

        assertThat(job.id()).isNotNull();
        assertThat(job.uploadId()).isEqualTo(uploadId);
        assertThat(job.userId()).isEqualTo(userId);
        assertThat(job.originalFilename()).isEqualTo("video.mp4");
        assertThat(job.sourceS3Key()).isEqualTo("uploads/key");
        assertThat(job.resultS3Key()).isNull();
        assertThat(job.status()).isEqualTo(ProcessingStatus.PENDING);
        assertThat(job.errorMessage()).isNull();
        assertThat(job.createdAt()).isNotNull();
        assertThat(job.updatedAt()).isNotNull();
    }

    @Test
    void started_shouldChangeStatusToProcessing() {
        ProcessingJob job = ProcessingJob.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", "key");
        ProcessingJob started = job.started();

        assertThat(started.status()).isEqualTo(ProcessingStatus.PROCESSING);
        assertThat(started.id()).isEqualTo(job.id());
        assertThat(started.errorMessage()).isNull();
    }

    @Test
    void completed_shouldSetStatusAndResultKey() {
        ProcessingJob job = ProcessingJob.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", "key");
        ProcessingJob completed = job.started().completed("processed/result.zip");

        assertThat(completed.status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(completed.resultS3Key()).isEqualTo("processed/result.zip");
        assertThat(completed.errorMessage()).isNull();
    }

    @Test
    void failed_shouldSetStatusAndErrorMessage() {
        ProcessingJob job = ProcessingJob.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", "key");
        ProcessingJob failed = job.started().failed("FFmpeg crashed");

        assertThat(failed.status()).isEqualTo(ProcessingStatus.FAILED);
        assertThat(failed.errorMessage()).isEqualTo("FFmpeg crashed");
    }

    @Test
    void recordAccessors_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProcessingJob job = new ProcessingJob(id, uploadId, userId, "test.mp4", "src",
                "res", ProcessingStatus.COMPLETED, "err", now, now);

        assertThat(job.id()).isEqualTo(id);
        assertThat(job.uploadId()).isEqualTo(uploadId);
        assertThat(job.userId()).isEqualTo(userId);
        assertThat(job.originalFilename()).isEqualTo("test.mp4");
        assertThat(job.sourceS3Key()).isEqualTo("src");
        assertThat(job.resultS3Key()).isEqualTo("res");
        assertThat(job.status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(job.errorMessage()).isEqualTo("err");
        assertThat(job.createdAt()).isEqualTo(now);
        assertThat(job.updatedAt()).isEqualTo(now);
    }
}
