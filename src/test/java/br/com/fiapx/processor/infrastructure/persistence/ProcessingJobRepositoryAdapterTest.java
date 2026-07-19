package br.com.fiapx.processor.infrastructure.persistence;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingJobRepositoryAdapterTest {

    @Mock
    private ProcessingJobJpaRepository jpaRepository;

    private ProcessingJobRepositoryAdapter adapter;

    private ProcessingJob sampleJob;

    @BeforeEach
    void setUp() {
        adapter = new ProcessingJobRepositoryAdapter(jpaRepository);
        sampleJob = new ProcessingJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "video.mp4", "src/key", "res/key", ProcessingStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_shouldPersistAndReturnDomainObject() {
        ProcessingJobEntity entity = ProcessingJobEntity.fromDomain(sampleJob);
        when(jpaRepository.save(any())).thenReturn(entity);

        ProcessingJob result = adapter.save(sampleJob);

        assertThat(result.id()).isEqualTo(sampleJob.id());
        verify(jpaRepository).save(any(ProcessingJobEntity.class));
    }

    @Test
    void findById_shouldReturnJob_whenExists() {
        ProcessingJobEntity entity = ProcessingJobEntity.fromDomain(sampleJob);
        when(jpaRepository.findById(sampleJob.id())).thenReturn(Optional.of(entity));

        Optional<ProcessingJob> result = adapter.findById(sampleJob.id());

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(sampleJob.id());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());

        Optional<ProcessingJob> result = adapter.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUploadId_shouldReturnJob() {
        ProcessingJobEntity entity = ProcessingJobEntity.fromDomain(sampleJob);
        when(jpaRepository.findByUploadId(sampleJob.uploadId())).thenReturn(Optional.of(entity));

        Optional<ProcessingJob> result = adapter.findByUploadId(sampleJob.uploadId());

        assertThat(result).isPresent();
    }

    @Test
    void findByUserId_shouldReturnList() {
        ProcessingJobEntity entity = ProcessingJobEntity.fromDomain(sampleJob);
        when(jpaRepository.findByUserId(sampleJob.userId())).thenReturn(List.of(entity));

        List<ProcessingJob> result = adapter.findByUserId(sampleJob.userId());

        assertThat(result).hasSize(1);
    }
}
