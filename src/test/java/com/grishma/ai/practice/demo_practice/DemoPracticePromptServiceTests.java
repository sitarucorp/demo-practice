package com.grishma.ai.practice.demo_practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class DemoPracticePromptServiceTests {

    private final PromptClient promptClient = Mockito.mock(PromptClient.class);
    private final AuditPromptUsageRepository auditPromptUsageRepository = Mockito.mock(AuditPromptUsageRepository.class);
    private final DemoPracticePromptService demoPracticePromptService =
            new DemoPracticePromptService(promptClient, auditPromptUsageRepository, "llama3.2:1b");

    @Test
    void fetchResultBuildsPromptResultFromOllamaPromptResponseAndAuditsSuccess() {
        when(promptClient.generate("who_am_i")).thenReturn("I am an Ollama response");

        PracticePromptInput input = PracticePromptInput.builder()
                .source("dummy")
                .prompt("who_am_i")
                .build();

        PracticePromptResult result = demoPracticePromptService.fetchResult(input);

        assertThat(result.source()).isEqualTo("dummy");
        assertThat(result.agentName()).isEqualTo("llama3.2:1b");
        assertThat(result.result()).isEqualTo("I am an Ollama response");
        verify(promptClient).generate("who_am_i");

        ArgumentCaptor<AuditPromptUsage> auditCaptor = ArgumentCaptor.forClass(AuditPromptUsage.class);
        verify(auditPromptUsageRepository).save(auditCaptor.capture());
        AuditPromptUsage auditPromptUsage = auditCaptor.getValue();

        assertThat(auditPromptUsage.getApiCall()).isEqualTo("GET /prompt/result");
        assertThat(auditPromptUsage.getCalledAt()).isNotNull();
        assertThat(auditPromptUsage.getSource()).isEqualTo("dummy");
        assertThat(auditPromptUsage.getAgentName()).isEqualTo("llama3.2:1b");
        assertThat(auditPromptUsage.getPrompt()).isEqualTo("who_am_i");
        assertThat(auditPromptUsage.getResult()).isEqualTo("I am an Ollama response");
        assertThat(auditPromptUsage.isSuccess()).isTrue();
    }

    @Test
    void fetchResultAuditsFailureBeforeRethrowingPromptClientError() {
        when(promptClient.generate("who_am_i")).thenThrow(new IllegalStateException("Ollama unavailable"));

        PracticePromptInput input = PracticePromptInput.builder()
                .source("dummy")
                .prompt("who_am_i")
                .build();

        assertThatThrownBy(() -> demoPracticePromptService.fetchResult(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ollama unavailable");

        ArgumentCaptor<AuditPromptUsage> auditCaptor = ArgumentCaptor.forClass(AuditPromptUsage.class);
        verify(auditPromptUsageRepository).save(auditCaptor.capture());
        AuditPromptUsage auditPromptUsage = auditCaptor.getValue();

        assertThat(auditPromptUsage.getApiCall()).isEqualTo("GET /prompt/result");
        assertThat(auditPromptUsage.getCalledAt()).isNotNull();
        assertThat(auditPromptUsage.getSource()).isEqualTo("dummy");
        assertThat(auditPromptUsage.getAgentName()).isEqualTo("llama3.2:1b");
        assertThat(auditPromptUsage.getPrompt()).isEqualTo("who_am_i");
        assertThat(auditPromptUsage.getResult()).isEqualTo("Ollama unavailable");
        assertThat(auditPromptUsage.isSuccess()).isFalse();
    }
}
