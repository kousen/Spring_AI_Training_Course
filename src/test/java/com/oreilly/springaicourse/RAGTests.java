package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
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
    private RelevancyEvaluator relevancyEvaluator;
    
    @BeforeEach
    void setUp() {
        // Create a separate ChatClient for evaluating responses
        evaluatorClient = ChatClient.create(openAiModel);
        
        // Create RelevancyEvaluator for testing RAG response quality
        relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(openAiModel));
    }
    
    /**
     * Helper method to evaluate if a response is relevant using Spring AI's RelevancyEvaluator
     */
    private void evaluateRelevancy(String question, ChatResponse chatResponse) {
        EvaluationRequest evaluationRequest = new EvaluationRequest(
            question,
            chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS),
            chatResponse.getResult().getOutput().getText()
        );
        
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertTrue(evaluationResponse.isPass(), 
            "Response should be relevant to the question. Evaluation details: " + evaluationResponse);
    }

    @Test
    void ragFromWikipediaInfo() {
        // Query about Spring (should return relevant info)
        String question = "What is the latest version of the Spring Framework?";
        ChatResponse chatResponse = ragService.queryWithResponse(question);
        String response = chatResponse.getResult().getOutput().getText();

        System.out.println("RAG Response about Spring:");
        System.out.println(response);

        // Basic assertions
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        // Use Spring AI's RelevancyEvaluator to validate response quality
        evaluateRelevancy(question, chatResponse);
    }

    @Test
    void ragFromPdfInfo() {
        // Query about the World Economic Forum report
        String question = """
                What are the most transformative technology trends expected to
                reshape global labor markets by 2030, and how does AI rank among them?
                """;
        ChatResponse chatResponse = ragService.queryWithResponse(question);
        String response = chatResponse.getResult().getOutput().getText();

        System.out.println("\nRAG Response about WEF Report:");
        System.out.println(response);

        // Basic assertions
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        // Use Spring AI's RelevancyEvaluator to validate response quality
        evaluateRelevancy(question, chatResponse);
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
        ChatResponse chatResponse = ragService.queryWithResponse(question);
        String response = chatResponse.getResult().getOutput().getText();

        System.out.println("\nRAG Response about Rap Beef:");
        System.out.println(response);

        // Basic assertions
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        // Use Spring AI's RelevancyEvaluator to validate response quality
        evaluateRelevancy(question, chatResponse);
    }
}