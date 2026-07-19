package br.com.fiapx.processor.infrastructure.ffmpeg;

import br.com.fiapx.processor.application.port.output.VideoProcessorPort;
import br.com.fiapx.processor.domain.exception.VideoProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FfmpegVideoProcessorAdapter implements VideoProcessorPort {

    private static final Logger log = LoggerFactory.getLogger(FfmpegVideoProcessorAdapter.class);

    @Override
    public Path extractFramesToZip(Path videoFile, String jobId) {
        try {
            Path framesDir = videoFile.getParent().resolve("frames");
            Files.createDirectories(framesDir);

            String pattern = framesDir.resolve("frame_%04d.jpg").toString();
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", videoFile.toString(),
                    "-vf", "fps=1",
                    "-q:v", "2",
                    pattern);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("FFmpeg failed with exit code {}: {}", exitCode, output);
                throw new VideoProcessingException("FFmpeg processing failed: " + output);
            }

            Path zipPath = videoFile.getParent().resolve("frames_" + jobId + ".zip");
            createZip(framesDir, zipPath);
            return zipPath;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoProcessingException("Video processing interrupted", e);
        } catch (VideoProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw new VideoProcessingException("Failed to process video", e);
        }
    }

    private void createZip(Path sourceDir, Path zipPath) throws IOException {
        File[] frames = sourceDir.toFile().listFiles((d, n) -> n.endsWith(".jpg"));
        if (frames == null || frames.length == 0) {
            throw new VideoProcessingException("No frames extracted from video");
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            for (File frame : frames) {
                zos.putNextEntry(new ZipEntry(frame.getName()));
                zos.write(Files.readAllBytes(frame.toPath()));
                zos.closeEntry();
            }
        }
    }
}
