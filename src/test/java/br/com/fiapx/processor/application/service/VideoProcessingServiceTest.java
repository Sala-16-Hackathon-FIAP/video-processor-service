package br.com.fiapx.processor.application.service;

import br.com.fiapx.processor.application.port.output.EventPublisherPort;
import br.com.fiapx.processor.application.port.output.ProcessingJobRepositoryPort;
import br.com.fiapx.processor.application.port.output.StoragePort;
import br.com.fiapx.processor.application.port.output.VideoProcessorPort;
import br.com.fiapx.processor.domain.exception.ProcessingJobNotFoundException;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoProcessingServiceTest {

    @Mock private ProcessingJobRepositoryPort jobRepository;
    @Mock private StoragePort storage;
    @Mock private VideoProcessorPort videoProcessor;
    @Mock private EventPublisherPort eventPublisher;

    @InjectMocks
    private VideoProcessingService service;

    @TempDir
    Path tempDir;

    private UUID userId;
    private UUID uploadId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        uploadId = UUID.randomUUID();
    }

    @Test
    void processVideo_shouldCompleteSuccessfully() throws IOException {
        Path zipFile = tempDir.resolve("result.zip");
        Files.createFile(zipFile);

        ArgumentCaptor<ProcessingJob> captor = ArgumentCaptor.forClass(ProcessingJob.class);
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(videoProcessor.extractFramesToZip(any(), any())).thenReturn(zipFile);

        service.processVideo(uploadId, userId, "video.mp4", "uploads/key");

        verify(eventPublisher).publishProcessingCompleted(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ProcessingStatus.COMPLETED);
        assertThat(captor.getValue().resultS3Key()).isNotNull();
        verify(eventPublisher).publishProcessingStarted(any());
        verify(storage).downloadToFile(any(), any());
        verify(storage).uploadFile(any(), eq(zipFile), eq("application/zip"));
    }

    @Test
    void processVideo_shouldFailWhenFFmpegFails() {
        ArgumentCaptor<ProcessingJob> captor = ArgumentCaptor.forClass(ProcessingJob.class);
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(videoProcessor.extractFramesToZip(any(), any()))
                .thenThrow(new RuntimeException("FFmpeg failed"));

        service.processVideo(uploadId, userId, "video.mp4", "uploads/key");

        verify(eventPublisher).publishProcessingFailed(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ProcessingStatus.FAILED);
        assertThat(captor.getValue().errorMessage()).contains("FFmpeg failed");
        verify(eventPublisher).publishProcessingStarted(any());
        verify(eventPublisher, never()).publishProcessingCompleted(any());
    }

    @Test
    void processVideo_shouldFailWhenStorageDownloadFails() {
        ArgumentCaptor<ProcessingJob> captor = ArgumentCaptor.forClass(ProcessingJob.class);
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("S3 error")).when(storage).downloadToFile(any(), any());

        service.processVideo(uploadId, userId, "video.mp4", "uploads/key");

        verify(eventPublisher).publishProcessingFailed(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ProcessingStatus.FAILED);
    }

    @Test
    void getJob_shouldReturnJob_whenExists() {
        ProcessingJob job = new ProcessingJob(UUID.randomUUID(), uploadId, userId, "v.mp4",
                "key", null, ProcessingStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now());
        when(jobRepository.findById(job.id())).thenReturn(Optional.of(job));

        ProcessingJob found = service.getJob(job.id());
        assertThat(found).isEqualTo(job);
    }

    @Test
    void getJob_shouldThrow_whenNotFound() {
        when(jobRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getJob(UUID.randomUUID()))
                .isInstanceOf(ProcessingJobNotFoundException.class);
    }

    @Test
    void getUserJobs_shouldReturnListForUser() {
        ProcessingJob job = new ProcessingJob(UUID.randomUUID(), uploadId, userId, "v.mp4",
                "key", null, ProcessingStatus.COMPLETED, null, LocalDateTime.now(), LocalDateTime.now());
        when(jobRepository.findByUserId(userId)).thenReturn(List.of(job));

        List<ProcessingJob> jobs = service.getUserJobs(userId);
        assertThat(jobs).hasSize(1);
    }
}
