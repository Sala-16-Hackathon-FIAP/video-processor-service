package br.com.fiapx.processor.infrastructure.s3;

import br.com.fiapx.processor.application.port.output.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageAdapter(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void downloadToFile(String s3Key, Path destination) {
        s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
                destination);
    }

    @Override
    public String uploadFile(String key, Path source, String contentType) {
        try {
            s3Client.putObject(r -> r.bucket(bucketName).key(key).contentType(contentType),
                    RequestBody.fromFile(source));
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + key, e);
        }
    }

    @Override
    public void deleteFile(String s3Key) {
        s3Client.deleteObject(r -> r.bucket(bucketName).key(s3Key));
    }
}
