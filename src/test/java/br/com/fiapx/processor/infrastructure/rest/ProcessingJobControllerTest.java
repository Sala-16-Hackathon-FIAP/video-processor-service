package br.com.fiapx.processor.infrastructure.rest;

import br.com.fiapx.processor.application.port.input.VideoProcessingUseCase;
import br.com.fiapx.processor.domain.exception.ProcessingJobNotFoundException;
import br.com.fiapx.processor.domain.model.ProcessingJob;
import br.com.fiapx.processor.domain.model.ProcessingStatus;
import br.com.fiapx.processor.infrastructure.rest.handler.GlobalExceptionHandler;
import br.com.fiapx.processor.infrastructure.security.JwtAuthFilter;
import br.com.fiapx.processor.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessingJobController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ProcessingJobControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean VideoProcessingUseCase processingUseCase;
    @MockBean JwtAuthFilter jwtAuthFilter;

    private UUID userId;
    private ProcessingJob sampleJob;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        sampleJob = new ProcessingJob(UUID.randomUUID(), UUID.randomUUID(), userId, "video.mp4",
                "src/key", "res/key", ProcessingStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getJob_shouldReturn200_whenJobExists() throws Exception {
        when(processingUseCase.getJob(sampleJob.id())).thenReturn(sampleJob);

        mockMvc.perform(get("/api/v1/jobs/{id}", sampleJob.id())
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.originalFilename").value("video.mp4"));
    }

    @Test
    void getJob_shouldReturn404_whenNotFound() throws Exception {
        when(processingUseCase.getJob(any()))
                .thenThrow(new ProcessingJobNotFoundException(UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/jobs/{id}", UUID.randomUUID())
                        .with(authentication(userAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void listJobs_shouldReturnUserJobs() throws Exception {
        when(processingUseCase.getUserJobs(userId)).thenReturn(List.of(sampleJob));

        mockMvc.perform(get("/api/v1/jobs").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].originalFilename").value("video.mp4"));
    }
}
