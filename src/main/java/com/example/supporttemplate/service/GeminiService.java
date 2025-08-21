package com.example.supporttemplate.service;


import com.example.supporttemplate.model.GeminiRequest;
import com.example.supporttemplate.model.GeminiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

	private  final WebClient webClient;

	private final String apiKey;

	public GeminiService(@Value("${gemini.api.key}") String apiKey) {
		this.apiKey = apiKey;

		// Create a more robust WebClient
		this.webClient = WebClient.builder()
				.baseUrl("https://generativelanguage.googleapis.com")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	public String chat(String context) {
		try {
		// Create the prompt
			String fullPrompt = String.format(
					"Create a professional customer support response template for the following issue type: '%s'. " +
							"Include placeholders like [CUSTOMER_NAME], [TICKET_NUMBER], [SPECIFIC_DETAILS] where appropriate. " +
							"Make it friendly, professional, and helpful. Keep it concise but comprehensive.",
					context
			);


		GeminiRequest.Part requestPart=new GeminiRequest.Part(fullPrompt);
		GeminiRequest.Content requestContent=new GeminiRequest.Content(List.of(requestPart));
		GeminiRequest request=new GeminiRequest(List.of(requestContent));



		// Build the request body
		Map<String, Object> requestBody = new HashMap<>();
		Map<String, Object> content = new HashMap<>();
		Map<String, String> part = new HashMap<>();

		part.put("text", fullPrompt);
		content.put("parts", List.of(part));
		requestBody.put("contents", List.of(content));

		// Make the API request with more detailed error handling
		GeminiResponse response = webClient.post()
				.uri("/v1beta/models/gemini-1.5-flash-latest:generateContent?key={apiKey}", apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(GeminiResponse.class)
				.block();



			if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
				return response.getCandidates().get(0).getContent().getParts().get(0).getText()
						.trim() // Remove whitespace
						.replace("\"", ""); // Remove quotes if present
			}


		} catch (Exception e) {
			e.printStackTrace();
			return "General Error"; // Default fallback
		}

		return "General Error"; // Default fallback
	}
	public ResponseEntity<GeminiResponse> chat2(String context) {
		try {
			// Create the prompt
			String fullPrompt = String.format(
					context
			);


			GeminiRequest.Part requestPart=new GeminiRequest.Part(fullPrompt);
			GeminiRequest.Content requestContent=new GeminiRequest.Content(List.of(requestPart));
			GeminiRequest request=new GeminiRequest(List.of(requestContent));


			// Make the API request with more detailed error handling
			GeminiResponse response = webClient.post()
					.uri("/v1beta/models/gemini-1.5-flash-latest:generateContent?key={apiKey}", apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(request)
					.retrieve()
					.bodyToMono(GeminiResponse.class)
					.block();

			if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {

				return ResponseEntity.ok(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}
}