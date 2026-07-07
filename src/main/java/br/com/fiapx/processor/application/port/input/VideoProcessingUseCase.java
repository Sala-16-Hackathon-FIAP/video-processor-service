package br.com.fiapx.processor.application.port.input;

import br.com.fiapx.processor.domain.model.ProcessingJob;

import java.util.List;
import java.util.UUID;

public interface VideoProcessingUseCase {
    void processVideo(UUID uploadId, UUID userId, String filename, String sourceS3Key);
    ProcessingJob getJob(UUID jobId);
    List<ProcessingJob> getUserJobs(UUID userId);
}
