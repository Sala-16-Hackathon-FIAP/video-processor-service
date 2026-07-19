package br.com.fiapx.processor.infrastructure.rest.dto;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingJobResponseTest {

    @Test
    void fromDomain_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProcessingJob job = new ProcessingJob(id, uploadId, userId, "video.mp4", "src",
                "res", ProcessingStatus.COMPLETED, "err", now, now);

        ProcessingJobResponse response = ProcessingJobResponse.fromDomain(job);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.uploadId()).isEqualTo(uploadId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.originalFilename()).isEqualTo("video.mp4");
        assertThat(response.status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(response.resultS3Key()).isEqualTo("res");
        assertThat(response.errorMessage()).isEqualTo("err");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
