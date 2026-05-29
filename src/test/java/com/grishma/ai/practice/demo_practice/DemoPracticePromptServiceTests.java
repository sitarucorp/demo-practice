package com.grishma.ai.practice.demo_practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DemoPracticePromptServiceTests {

    private final PromptClient promptClient = Mockito.mock(PromptClient.class);
    private final DemoPracticePromptService demoPracticePromptService =
            new DemoPracticePromptService(promptClient, "llama3.2:1b");

    @Test
    void fetchResultBuildsPromptResultFromOllamaPromptResponse() {
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
    }
}
