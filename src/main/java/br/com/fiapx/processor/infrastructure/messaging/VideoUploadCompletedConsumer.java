package br.com.fiapx.processor.infrastructure.messaging;

import br.com.fiapx.processor.application.port.input.VideoProcessingUseCase;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class VideoUploadCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoUploadCompletedConsumer.class);

    static final String EXCHANGE = "fiapx.events";
    static final String QUEUE = "processor.video.upload.completed";
    static final String ROUTING_KEY = "video.upload.completed";

    private final TopicConsumer topicConsumer;
    private final VideoProcessingUseCase processingUseCase;

    public VideoUploadCompletedConsumer(TopicConsumer topicConsumer, VideoProcessingUseCase processingUseCase) {
        this.topicConsumer = topicConsumer;
        this.processingUseCase = processingUseCase;
    }

    @PostConstruct
    public void registerConsumer() {
        TopicBinding binding = new TopicBinding(EXCHANGE, ROUTING_KEY, QUEUE);
        topicConsumer.consume(binding, VideoUploadCompletedEvent.class, this::handle);
    }

    public void handle(VideoUploadCompletedEvent event) {
        log.info("Received upload.completed event for upload: {}", event.uploadId());
        processingUseCase.processVideo(event.uploadId(), event.userId(), event.filename(), event.s3Key());
    }

    public record VideoUploadCompletedEvent(
        UUID uploadId,
        UUID userId,
        String filename,
        String s3Key,
        String mimeType,
        LocalDateTime uploadedAt
    ) {}
}
