package br.com.fiapx.processor.domain.exception;

import java.util.UUID;

public class ProcessingJobNotFoundException extends RuntimeException {
    public ProcessingJobNotFoundException(UUID id) {
        super("Processing job not found: " + id);
    }
}
