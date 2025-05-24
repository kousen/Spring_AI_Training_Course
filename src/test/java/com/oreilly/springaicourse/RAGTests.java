package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"rag","redis"})
public class RAGTests {

    @Autowired
    private RAGService ragService;
    
    @Autowired
    private OpenAiChatModel openAiModel;
    
    private ChatClient evaluatorClient;
    
    @BeforeEach
    void setUp() {
        // Create a separate ChatClient for evaluating responses
        evaluatorClient = ChatClient.create(openAiModel);
    }

    @Test
    void ragFromWikipediaInfo() {
        // Query about Spring (should return relevant info)
        String question = "What is the latest version of the Spring Framework?";
        String response = ragService.query(question);

        System.out.println("RAG Response about Spring:");
        System.out.println(response);

        // Assertions for Chat Client API query
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void ragFromPdfInfo() {
        // Query about the World Economic Forum report
        String question = """
                What are the most transformative technology trends expected to
                reshape global labor markets by 2030, and how does AI rank among them?
                """;
        String response = ragService.query(question);

        System.out.println("\nRAG Response about WEF Report:");
        System.out.println(response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void outOfScopeQuery() {
        String outOfScopeQuestion = "How do I implement GraphQL in Spring?";
        String outOfScopeResponse = ragService.query(outOfScopeQuestion);

        System.out.println("\nOut of scope RAG Response:");
        System.out.println(outOfScopeResponse);

        assertNotNull(outOfScopeResponse);
        
        // Use AI to evaluate if the response properly indicates lack of knowledge
        String evaluationPrompt = String.format("""
            Does the following response properly indicate that the system doesn't have enough 
            information to answer the question, or that the question is outside its knowledge base?
            
            Response to evaluate: "%s"
            
            Answer with only "true" or "false".
            """, outOfScopeResponse.replace("\"", "\\\""));
            
        String evaluation = evaluatorClient.prompt(evaluationPrompt).call().content();
        
        assertTrue(
            evaluation.trim().toLowerCase().contains("true"),
            "AI evaluation failed - Response should indicate lack of information. " +
            "Evaluation: " + evaluation + ", Original response: " + outOfScopeResponse
        );
    }

    @Test
    void domainSpecificQuery() {
        String question = "Who won the Kendrick Lamar / Drake feud?";
        String response = ragService.query(question);

        System.out.println("\nRAG Response about Rap Beef:");
        System.out.println(response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}