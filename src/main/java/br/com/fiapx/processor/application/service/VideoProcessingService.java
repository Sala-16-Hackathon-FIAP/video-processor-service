package br.com.fiapx.processor.application.service;

import br.com.fiapx.processor.application.port.input.VideoProcessingUseCase;
import br.com.fiapx.processor.application.port.output.EventPublisherPort;
import br.com.fiapx.processor.application.port.output.ProcessingJobRepositoryPort;
import br.com.fiapx.processor.application.port.output.StoragePort;
import br.com.fiapx.processor.application.port.output.VideoProcessorPort;
import br.com.fiapx.processor.domain.exception.ProcessingJobNotFoundException;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoProcessingService implements VideoProcessingUseCase {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessingService.class);

    private final ProcessingJobRepositoryPort jobRepository;
    private final StoragePort storage;
    private final VideoProcessorPort videoProcessor;
    private final EventPublisherPort eventPublisher;

    public VideoProcessingService(
            ProcessingJobRepositoryPort jobRepository,
            StoragePort storage,
            VideoProcessorPort videoProcessor,
            EventPublisherPort eventPublisher) {
        this.jobRepository = jobRepository;
        this.storage = storage;
        this.videoProcessor = videoProcessor;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes a video synchronously on the RabbitMQ listener thread. The broker
     * message is only acknowledged after this method returns, so a pod crash mid-way
     * leaves the message unacknowledged and it is redelivered (at-least-once).
     * Concurrency across videos comes from replicas + listener consumers, not from
     * an async executor. Idempotent per uploadId: a duplicate/redelivered event for an
     * already-completed upload is skipped; any other prior state is reprocessed on the
     * same job row (upload_id is UNIQUE in the database).
     */
    @Override
    public void processVideo(UUID uploadId, UUID userId, String filename, String sourceS3Key) {
        Optional<ProcessingJob> existing = jobRepository.findByUploadId(uploadId);
        if (existing.isPresent() && existing.get().status() == ProcessingStatus.COMPLETED) {
            log.info("Upload {} already processed (job {} COMPLETED); skipping duplicate event",
                    uploadId, existing.get().id());
            return;
        }

        ProcessingJob job = existing.orElseGet(
                () -> jobRepository.save(ProcessingJob.create(uploadId, userId, filename, sourceS3Key)));

        job = job.started();
        job = jobRepository.save(job);
        eventPublisher.publishProcessingStarted(job);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("fiapx-processor-" + job.id());
            Path videoFile = tempDir.resolve(filename);

            log.info("Downloading video from S3: {}", sourceS3Key);
            storage.downloadToFile(sourceS3Key, videoFile);

            log.info("Extracting frames from video: {}", videoFile);
            Path zipFile = videoProcessor.extractFramesToZip(videoFile, job.id().toString());

            String resultKey = "processed/" + userId + "/" + job.id() + "/frames.zip";
            log.info("Uploading result to S3: {}", resultKey);
            storage.uploadFile(resultKey, zipFile, "application/zip");

            job = job.completed(resultKey);
            job = jobRepository.save(job);
            eventPublisher.publishProcessingCompleted(job);

            log.info("Video processing completed for job: {}", job.id());

        } catch (Exception e) {
            log.error("Video processing failed for job: {}", job.id(), e);
            job = job.failed(e.getMessage());
            job = jobRepository.save(job);
            eventPublisher.publishProcessingFailed(job);
        } finally {
            cleanupTemp(tempDir);
        }
    }

    @Override
    public ProcessingJob getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ProcessingJobNotFoundException(jobId));
    }

    @Override
    public List<ProcessingJob> getUserJobs(UUID userId) {
        return jobRepository.findByUserId(userId);
    }

    private void cleanupTemp(Path dir) {
        if (dir == null) return;
        try {
            try (var walk = Files.walk(dir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup temp directory: {}", dir, e);
        }
    }
}
