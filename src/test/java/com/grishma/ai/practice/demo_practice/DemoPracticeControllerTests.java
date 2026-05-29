package com.grishma.ai.practice.demo_practice;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DemoPracticeController.class)
class DemoPracticeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemoPracticePromptService demoPracticePromptService;

    @Test
    void promptResultReturnsServiceResponse() throws Exception {
        when(demoPracticePromptService.fetchResult(any(PracticePromptInput.class)))
                .thenReturn(PracticePromptResult.builder()
                        .source("dummy")
                        .agentName("llama3.2:1b")
                        .result("I am an Ollama response")
                        .build());

        mockMvc.perform(get("/prompt/result")
                        .param("source", "dummy")
                        .param("prompt", "who_am_i"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source", equalTo("dummy")))
                .andExpect(jsonPath("$.agentName", equalTo("llama3.2:1b")))
                .andExpect(jsonPath("$.result", equalTo("I am an Ollama response")));

        ArgumentCaptor<PracticePromptInput> inputCaptor = ArgumentCaptor.forClass(PracticePromptInput.class);
        verify(demoPracticePromptService).fetchResult(inputCaptor.capture());
        PracticePromptInput input = inputCaptor.getValue();

        org.assertj.core.api.Assertions.assertThat(input.source()).isEqualTo("dummy");
        org.assertj.core.api.Assertions.assertThat(input.prompt()).isEqualTo("who_am_i");
    }

    @Test
    void promptResultRequiresSourceParameter() throws Exception {
        mockMvc.perform(get("/prompt/result")
                        .param("prompt", "who_am_i"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void promptResultRequiresPromptParameter() throws Exception {
        mockMvc.perform(get("/prompt/result")
                        .param("source", "dummy"))
                .andExpect(status().isBadRequest());
    }
}
