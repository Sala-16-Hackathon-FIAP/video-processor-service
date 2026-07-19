package br.com.fiapx.processor.infrastructure.ffmpeg;

import br.com.fiapx.processor.domain.exception.VideoProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FfmpegVideoProcessorAdapterTest {

    private FfmpegVideoProcessorAdapter adapter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        adapter = new FfmpegVideoProcessorAdapter();
    }

    @Test
    void extractFramesToZip_shouldThrow_whenFfmpegIsNotAvailable() throws IOException {
        Path fakeVideo = tempDir.resolve("fake.mp4");
        Files.write(fakeVideo, new byte[]{0x00, 0x01, 0x02});

        assertThatThrownBy(() -> adapter.extractFramesToZip(fakeVideo, "job-123"))
                .isInstanceOf(VideoProcessingException.class);
    }

    @Test
    void extractFramesToZip_shouldThrow_whenVideoFileDoesNotExist() {
        Path nonExistentFile = tempDir.resolve("nonexistent.mp4");

        assertThatThrownBy(() -> adapter.extractFramesToZip(nonExistentFile, "job-456"))
                .isInstanceOf(VideoProcessingException.class);
    }
}
