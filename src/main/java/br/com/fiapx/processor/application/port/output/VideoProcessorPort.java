package br.com.fiapx.processor.application.port.output;

import java.nio.file.Path;

public interface VideoProcessorPort {
    Path extractFramesToZip(Path videoFile, String jobId);
}
