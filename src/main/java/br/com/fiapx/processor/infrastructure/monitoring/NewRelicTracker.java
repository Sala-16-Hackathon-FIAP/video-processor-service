package br.com.fiapx.processor.infrastructure.monitoring;

import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NewRelicTracker {

    public void trackJobStarted(UUID jobId) {
        NewRelic.getAgent().getInsights().recordCustomEvent("VideoProcessingStarted", Map.of("jobId", jobId.toString()));
        NewRelic.incrementCounter("Custom/VideoProcessing/Started");
    }

    public void trackJobCompleted(UUID jobId, long durationMs) {
        NewRelic.getAgent().getInsights().recordCustomEvent("VideoProcessingCompleted", Map.of(
                "jobId", jobId.toString(), "durationMs", String.valueOf(durationMs)));
        NewRelic.incrementCounter("Custom/VideoProcessing/Completed");
    }

    public void trackJobFailed(UUID jobId, String reason) {
        NewRelic.getAgent().getInsights().recordCustomEvent("VideoProcessingFailed", Map.of(
                "jobId", jobId.toString(), "reason", reason != null ? reason : "unknown"));
        NewRelic.incrementCounter("Custom/VideoProcessing/Failed");
    }
}
