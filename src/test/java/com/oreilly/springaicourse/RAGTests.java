package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"rag", "redis"})  // Enable RAG and Redis profiles for testing
class RAGTests {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RAGService ragService;

    // === Advanced RAG Testing ===

    @Test
    void testWikipediaRAG() {
        // TODO: Test RAG with Wikipedia content
        // 1. Create a ChatClient with QuestionAnswerAdvisor
        // 2. Ask a question about Spring Framework
        // 3. Verify the response uses retrieved context
        // 4. Print the response
    }

    @Test
    void testPdfRAG() {
        // TODO: Test RAG with PDF content  
        // 1. Ask a question about content from the PDF documents
        // 2. Verify the response references the PDF content
        // 3. Print the response showing context usage
    }

    @Test
    void testOutOfScopeQuery() {
        // TODO: Test behavior with questions outside the knowledge base
        // 1. Ask a question unrelated to the ingested documents
        // 2. Verify the system appropriately indicates when context isn't available
        // 3. Compare response quality with and without RAG
    }

    @Test
    void testRelevancyEvaluation() {
        // TODO: Implement relevancy evaluation for RAG responses
        // 1. Create a RelevancyEvaluator
        // 2. Generate a response using RAG
        // 3. Evaluate the relevancy of the response to the question
        // 4. Print evaluation results and scores
        // 5. Assert that relevancy meets minimum threshold
    }

    @Test
    void testVectorStoreOperations() {
        // TODO: Test basic vector store operations
        // 1. Verify documents are properly stored in the vector store
        // 2. Test similarity search functionality
        // 3. Print search results and similarity scores
    }

    @Test
    void testRAGServiceInteraction() {
        // TODO: Test the RAGService class
        // 1. Use RAGService to ask questions
        // 2. Verify responses include context from stored documents
        // 3. Test the service's chat functionality
    }

}