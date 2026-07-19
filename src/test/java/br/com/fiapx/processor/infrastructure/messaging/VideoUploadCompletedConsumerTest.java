package br.com.fiapx.processor.infrastructure.messaging;

import br.com.fiapx.processor.application.port.input.VideoProcessingUseCase;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import com.autoflow.rabbit_topic_lib.model.TopicBinding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoUploadCompletedConsumerTest {

    @Mock private TopicConsumer topicConsumer;
    @Mock private VideoProcessingUseCase processingUseCase;

    @InjectMocks
    private VideoUploadCompletedConsumer consumer;

    @Test
    void registerConsumer_shouldBindToCorrectQueue() {
        consumer.registerConsumer();

        verify(topicConsumer).consume(any(TopicBinding.class),
                eq(VideoUploadCompletedConsumer.VideoUploadCompletedEvent.class), any());
    }

    @Test
    void handle_shouldTriggerVideoProcessing() {
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        VideoUploadCompletedConsumer.VideoUploadCompletedEvent event =
                new VideoUploadCompletedConsumer.VideoUploadCompletedEvent(
                        uploadId, userId, "video.mp4", "uploads/key", "video/mp4", LocalDateTime.now());

        consumer.handle(event);

        verify(processingUseCase).processVideo(
                eq(uploadId), eq(userId), eq("video.mp4"), eq("uploads/key"));
    }
}
