package com.grishma.ai.practice.demo_practice;

import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
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

    @Retryable
    public PracticePromptResult fetchResult(@NonNull PracticePromptInput practicePromptInput) {
        return PracticePromptResult.builder()
                .result("This is a test result")
                .agentName("strange-ollama")
                .source(practicePromptInput.source()).build();
    }
}

@Builder
record PracticePromptInput(String source, String prompt) {
};

@Builder
record PracticePromptResult(String source, String agentName, String result) {
};
