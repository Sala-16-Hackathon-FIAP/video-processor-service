package br.com.fiapx.processor.application.port.output;

import br.com.fiapx.processor.domain.model.ProcessingJob;

public interface EventPublisherPort {
    void publishProcessingStarted(ProcessingJob job);
    void publishProcessingCompleted(ProcessingJob job);
    void publishProcessingFailed(ProcessingJob job);
}
