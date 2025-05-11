package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("rag")
public class RAGTests {

    @Autowired
    private RAGService ragService;

    @Test
    void retrievalAugmentedGeneration() {
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
    void outOfScopeQuery() {
        // Query about something not in our knowledge base
        String outOfScopeQuestion = "How do I implement GraphQL in Spring?";
        String outOfScopeResponse = ragService.query(outOfScopeQuestion);

        System.out.println("\nOut of scope RAG Response:");
        System.out.println(outOfScopeResponse);

        // Assertions for out-of-scope query
        assertNotNull(outOfScopeResponse);
        assertTrue(outOfScopeResponse.contains("don't have enough information") ||
                outOfScopeResponse.contains("not enough information") ||
                outOfScopeResponse.contains("cannot provide information"),
                "Should indicate lack of information for out-of-scope questions");
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