package br.com.fiapx.processor.infrastructure.rest;

import br.com.fiapx.processor.application.port.input.VideoProcessingUseCase;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.infrastructure.rest.dto.ProcessingJobResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Processing Jobs", description = "Video processing job status")
public class ProcessingJobController {

    private final VideoProcessingUseCase processingUseCase;

    public ProcessingJobController(VideoProcessingUseCase processingUseCase) {
        this.processingUseCase = processingUseCase;
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get a processing job by ID")
    public ProcessingJobResponse getJob(@PathVariable UUID jobId) {
        return ProcessingJobResponse.fromDomain(processingUseCase.getJob(jobId));
    }

    @GetMapping
    @Operation(summary = "List all jobs for the authenticated user")
    public List<ProcessingJobResponse> listJobs(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return processingUseCase.getUserJobs(userId).stream()
                .map(ProcessingJobResponse::fromDomain).toList();
    }
}
