package br.com.fiapx.processor.infrastructure.messaging;

import br.com.fiapx.processor.application.port.output.EventPublisherPort;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import com.autoflow.rabbit_topic_lib.core.TopicPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ProcessingEventPublisher implements EventPublisherPort {

    static final String EXCHANGE = "fiapx.events";
    static final String KEY_STARTED = "video.processing.started";
    static final String KEY_COMPLETED = "video.processing.completed";
    static final String KEY_FAILED = "video.processing.failed";

    private final TopicPublisher topicPublisher;

    public ProcessingEventPublisher(TopicPublisher topicPublisher) {
        this.topicPublisher = topicPublisher;
    }

    @Override
    public void publishProcessingStarted(ProcessingJob job) {
        topicPublisher.publish(EXCHANGE, KEY_STARTED, toEvent(job));
    }

    @Override
    public void publishProcessingCompleted(ProcessingJob job) {
        topicPublisher.publish(EXCHANGE, KEY_COMPLETED, toEvent(job));
    }

    @Override
    public void publishProcessingFailed(ProcessingJob job) {
        topicPublisher.publish(EXCHANGE, KEY_FAILED, toEvent(job));
    }

    private ProcessingJobEvent toEvent(ProcessingJob job) {
        return new ProcessingJobEvent(job.id(), job.uploadId(), job.userId(),
                job.originalFilename(), job.resultS3Key(), job.status().name(),
                job.errorMessage(), LocalDateTime.now());
    }

    public record ProcessingJobEvent(
        UUID jobId,
        UUID uploadId,
        UUID userId,
        String filename,
        String resultS3Key,
        String status,
        String errorMessage,
        LocalDateTime timestamp
    ) {}
}
