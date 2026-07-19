package br.com.fiapx.processor.infrastructure.messaging;

import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import com.autoflow.rabbit_topic_lib.core.TopicPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessingEventPublisherTest {

    @Mock
    private TopicPublisher topicPublisher;

    @InjectMocks
    private ProcessingEventPublisher publisher;

    private ProcessingJob sampleJob() {
        return new ProcessingJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "video.mp4", "src/key", "res/key", ProcessingStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void publishProcessingStarted_shouldPublishWithCorrectRoutingKey() {
        ProcessingJob job = sampleJob();
        publisher.publishProcessingStarted(job);

        ArgumentCaptor<ProcessingEventPublisher.ProcessingJobEvent> captor =
                ArgumentCaptor.forClass(ProcessingEventPublisher.ProcessingJobEvent.class);
        verify(topicPublisher).publish(eq("fiapx.events"), eq("video.processing.started"), captor.capture());

        assertThat(captor.getValue().jobId()).isEqualTo(job.id());
        assertThat(captor.getValue().uploadId()).isEqualTo(job.uploadId());
    }

    @Test
    void publishProcessingCompleted_shouldPublishWithCorrectRoutingKey() {
        ProcessingJob job = sampleJob();
        publisher.publishProcessingCompleted(job);

        verify(topicPublisher).publish(eq("fiapx.events"), eq("video.processing.completed"),
                org.mockito.ArgumentMatchers.any(ProcessingEventPublisher.ProcessingJobEvent.class));
    }

    @Test
    void publishProcessingFailed_shouldPublishWithCorrectRoutingKey() {
        ProcessingJob job = sampleJob();
        publisher.publishProcessingFailed(job);

        verify(topicPublisher).publish(eq("fiapx.events"), eq("video.processing.failed"),
                org.mockito.ArgumentMatchers.any(ProcessingEventPublisher.ProcessingJobEvent.class));
    }
}
