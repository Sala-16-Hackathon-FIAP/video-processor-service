package br.com.fiapx.processor.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingJobNotFoundExceptionTest {

    @Test
    void shouldContainJobIdInMessage() {
        UUID id = UUID.randomUUID();
        ProcessingJobNotFoundException ex = new ProcessingJobNotFoundException(id);
        assertThat(ex.getMessage()).contains(id.toString());
        assertThat(ex.getMessage()).contains("Processing job not found");
    }
}
