package br.com.fiapx.processor.infrastructure.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    private S3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3StorageAdapter(s3Client, "test-bucket");
    }

    @Test
    void downloadToFile_shouldCallS3GetObject(@TempDir Path tempDir) {
        Path destination = tempDir.resolve("video.mp4");
        adapter.downloadToFile("uploads/key", destination);

        verify(s3Client).getObject(any(GetObjectRequest.class), eq(destination));
    }

    @Test
    void uploadFile_shouldCallS3PutObject(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("frames.zip");
        Files.write(source, new byte[]{1, 2, 3});

        PutObjectResponse response = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(Consumer.class), any(RequestBody.class))).thenReturn(response);

        String result = adapter.uploadFile("processed/key", source, "application/zip");

        assertThat(result).isEqualTo("processed/key");
        verify(s3Client).putObject(any(Consumer.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_shouldThrowRuntimeException_whenS3Fails(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("frames.zip");
        Files.write(source, new byte[]{1, 2, 3});

        when(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThatThrownBy(() -> adapter.uploadFile("key", source, "application/zip"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload file to S3");
    }

    @Test
    void deleteFile_shouldCallS3DeleteObject() {
        adapter.deleteFile("uploads/key");

        verify(s3Client).deleteObject(any(Consumer.class));
    }
}
