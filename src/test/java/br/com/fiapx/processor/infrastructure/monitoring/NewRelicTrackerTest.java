package br.com.fiapx.processor.infrastructure.monitoring;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NewRelicTrackerTest {

    @Test
    void trackJobStarted_shouldRecordEventAndIncrementCounter() {
        NewRelicTracker tracker = new NewRelicTracker();
        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackJobStarted(UUID.randomUUID());

            verify(insights).recordCustomEvent(eq("VideoProcessingStarted"), anyMap());
            mocked.verify(() -> NewRelic.incrementCounter("Custom/VideoProcessing/Started"));
        }
    }

    @Test
    void trackJobCompleted_shouldRecordEventAndIncrementCounter() {
        NewRelicTracker tracker = new NewRelicTracker();
        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackJobCompleted(UUID.randomUUID(), 5000L);

            verify(insights).recordCustomEvent(eq("VideoProcessingCompleted"), anyMap());
            mocked.verify(() -> NewRelic.incrementCounter("Custom/VideoProcessing/Completed"));
        }
    }

    @Test
    void trackJobFailed_shouldRecordEventAndIncrementCounter() {
        NewRelicTracker tracker = new NewRelicTracker();
        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackJobFailed(UUID.randomUUID(), "error reason");

            verify(insights).recordCustomEvent(eq("VideoProcessingFailed"), anyMap());
            mocked.verify(() -> NewRelic.incrementCounter("Custom/VideoProcessing/Failed"));
        }
    }

    @Test
    void trackJobFailed_shouldHandleNullReason() {
        NewRelicTracker tracker = new NewRelicTracker();
        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackJobFailed(UUID.randomUUID(), null);

            verify(insights).recordCustomEvent(eq("VideoProcessingFailed"), anyMap());
        }
    }
}
