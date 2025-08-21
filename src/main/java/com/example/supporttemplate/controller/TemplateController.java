package com.example.supporttemplate.controller;

import com.example.supporttemplate.model.GeminiResponse;
import com.example.supporttemplate.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemplateController {
    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateTemplate(@RequestBody Map<String, String> request) {
        try {
            // Get the issue type from the request
            String issueType = request.get("issueType");

            // Validate the input
            if (issueType == null || issueType.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Issue type is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Generate the template using Gemini
            String template = geminiService.chat(issueType);

            // Prepare and return the response
            Map<String, String> response = new HashMap<>();
            response.put("issueType", issueType);
            response.put("template", template);



            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Handle any errors
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate template: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    @PostMapping("/chat")
    public ResponseEntity<String> generateTemplate2(@RequestBody Map<String, Object> request) {
        try {
            // Get the message and context from the request
            String message = (String) request.get("issueType2");
            List<Map<String, String>> context = (List<Map<String, String>>) request.get("context");

            // Validate the input
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message is required");
            }

            // Build the prompt with context if available
            String prompt;
            if (context != null && !context.isEmpty()) {
                StringBuilder promptBuilder = new StringBuilder();
                for (Map<String, String> msg : context) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    promptBuilder.append(role).append(": ").append(content).append("\n");
                }
                promptBuilder.append("assistant: ");
                prompt = promptBuilder.toString();
            } else {
                prompt = message;
            }

            // Generate the response using Gemini
            GeminiResponse response = geminiService.chat2(prompt).getBody();
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                String answer = response.getCandidates().get(0).getContent().getParts().get(0).getText()
                        .trim()
                        .replace("\"", "");
                return ResponseEntity.ok(answer);
            }

            return ResponseEntity.ok("I couldn't generate a response. Please try again.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running!");
    }


}