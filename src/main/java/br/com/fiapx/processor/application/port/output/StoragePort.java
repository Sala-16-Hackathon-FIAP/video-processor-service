package br.com.fiapx.processor.application.port.output;

import java.io.InputStream;
import java.nio.file.Path;

public interface StoragePort {
    void downloadToFile(String s3Key, Path destination);
    String uploadFile(String key, Path source, String contentType);
    void deleteFile(String s3Key);
}
