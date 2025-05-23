package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
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
        String lower = outOfScopeResponse.trim().toLowerCase();
        List<String> oosPhrases = List.of(
            "not contain information",
            "not enough information",
            "can't answer",
            "i don't know",
            "i am not able",
            "sorry",
            "unable",
            "no information",
            "outside my knowledge"
        );
        assertTrue(
            oosPhrases.stream().anyMatch(lower::contains),
            "Should indicate lack of information for out-of-scope questions (actual: " + outOfScopeResponse + ")"
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