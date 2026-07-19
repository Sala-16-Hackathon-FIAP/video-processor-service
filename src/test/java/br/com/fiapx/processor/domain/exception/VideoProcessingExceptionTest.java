package br.com.fiapx.processor.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoProcessingExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        VideoProcessingException ex = new VideoProcessingException("FFmpeg failed");
        assertThat(ex.getMessage()).isEqualTo("FFmpeg failed");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        VideoProcessingException ex = new VideoProcessingException("FFmpeg failed", cause);
        assertThat(ex.getMessage()).isEqualTo("FFmpeg failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
