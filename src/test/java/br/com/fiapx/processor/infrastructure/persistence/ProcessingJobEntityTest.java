package br.com.fiapx.processor.infrastructure.persistence;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingJobEntityTest {

    @Test
    void fromDomainAndToDomain_shouldRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProcessingJob original = new ProcessingJob(id, uploadId, userId, "video.mp4",
                "src/key", "res/key", ProcessingStatus.COMPLETED, "error msg", now, now);

        ProcessingJobEntity entity = ProcessingJobEntity.fromDomain(original);
        ProcessingJob result = entity.toDomain();

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.uploadId()).isEqualTo(uploadId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.originalFilename()).isEqualTo("video.mp4");
        assertThat(result.sourceS3Key()).isEqualTo("src/key");
        assertThat(result.resultS3Key()).isEqualTo("res/key");
        assertThat(result.status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(result.errorMessage()).isEqualTo("error msg");
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }
}
