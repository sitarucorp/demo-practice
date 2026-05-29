package com.grishma.ai.practice.demo_practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class DemoPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoPracticeApplication.class, args);
    }

}

@RestController
@AllArgsConstructor
class DemoPracticeController {

    DemoPracticePromptService demoPracticePromptService;

    @GetMapping(path = "/prompt/result")
    public ResponseEntity<PracticePromptResult> prompt(@NonNull @RequestParam String source, @RequestParam String prompt) {
        return ResponseEntity.ok(demoPracticePromptService.fetchResult(PracticePromptInput.builder()
                .source(source)
                .prompt(prompt)
                .build()));
    }
}

@Service
class DemoPracticePromptService {

    private final PromptClient promptClient;

    private final String agentName;

    DemoPracticePromptService(PromptClient promptClient, @Value("${spring.ai.ollama.chat.model}") String agentName) {
        this.promptClient = promptClient;
        this.agentName = agentName;
    }

    @Retryable
    public PracticePromptResult fetchResult(@NonNull PracticePromptInput practicePromptInput) {
        return PracticePromptResult.builder()
                .result(promptClient.generate(practicePromptInput.prompt()))
                .agentName(agentName)
                .source(practicePromptInput.source()).build();
    }
}

interface PromptClient {

    String generate(String prompt);
}

@Component
class OllamaPromptClient implements PromptClient {

    private final ChatClient chatClient;

    OllamaPromptClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generate(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}

@Builder
record PracticePromptInput(String source, String prompt) {
};

@Builder
record PracticePromptResult(String source, String agentName, String result) {
};
