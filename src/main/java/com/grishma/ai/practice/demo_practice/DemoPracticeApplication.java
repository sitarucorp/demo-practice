package com.grishma.ai.practice.demo_practice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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

    private static final String PROMPT_RESULT_API_CALL = "GET /prompt/result";

    private final PromptClient promptClient;

    private final AuditPromptUsageRepository auditPromptUsageRepository;

    private final String agentName;

    DemoPracticePromptService(PromptClient promptClient,
                              AuditPromptUsageRepository auditPromptUsageRepository,
                              @Value("${spring.ai.ollama.chat.model}") String agentName) {
        this.promptClient = promptClient;
        this.auditPromptUsageRepository = auditPromptUsageRepository;
        this.agentName = agentName;
    }

    public PracticePromptResult fetchResult(@NonNull PracticePromptInput practicePromptInput) {
        try {
            String result = promptClient.generate(practicePromptInput.prompt());
            auditPromptUsageRepository.save(buildAuditPromptUsage(practicePromptInput, result, true));

            return PracticePromptResult.builder()
                    .result(result)
                    .agentName(agentName)
                    .source(practicePromptInput.source()).build();
        } catch (RuntimeException exception) {
            auditPromptUsageRepository.save(buildAuditPromptUsage(practicePromptInput, exception.getMessage(), false));
            throw exception;
        }
    }

    private AuditPromptUsage buildAuditPromptUsage(PracticePromptInput practicePromptInput,
                                                   String result,
                                                   boolean success) {
        return AuditPromptUsage.builder()
                .apiCall(PROMPT_RESULT_API_CALL)
                .calledAt(Instant.now())
                .source(practicePromptInput.source())
                .agentName(agentName)
                .prompt(practicePromptInput.prompt())
                .result(result)
                .success(success)
                .build();
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

interface AuditPromptUsageRepository extends JpaRepository<AuditPromptUsage, Long> {
}

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class AuditPromptUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String apiCall;

    @Column(nullable = false)
    private Instant calledAt;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String agentName;

    @Column(nullable = false, length = 4000)
    private String prompt;

    @Column(length = 4000)
    private String result;

    @Column(nullable = false)
    private boolean success;
}
