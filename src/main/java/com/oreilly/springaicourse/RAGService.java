package com.oreilly.springaicourse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private final ChatMemory memory;

    @Autowired
    public RAGService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore, ChatMemory memory) {
        this.chatClient = ChatClient.create(chatModel);
        this.vectorStore = vectorStore;
        this.memory = memory;
    }

    public String query(String question) {
        // Create a QuestionAnswerAdvisor with the vectorStore
        var questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);

        // Good to use chat memory when doing RAG
        var chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(memory).build();

        // Use the advisor to handle the RAG workflow
        return chatClient.prompt()
                .advisors(questionAnswerAdvisor, chatMemoryAdvisor)
                .user(question)
                .call()
                .content();
    }

    public static void main(String[] args) {
        // Create a Spring application instance
        var app = new SpringApplication(SpringaicourseApplication.class);

        // Set the active profiles to "rag" and "redis"
        app.setAdditionalProfiles("rag", "redis");

        // Start the Spring application and get the application context
        ApplicationContext context = app.run(args);

        // Get the RAGService bean from the context
        RAGService ragService = context.getBean(RAGService.class);

        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        System.out.println("RAG Question-Answering System");
        System.out.println("Type 'exit' to quit");
        System.out.println("------------------------------");

        // Loop to ask questions until user types 'exit'
        while (true) {
            System.out.print("\nEnter your question: ");
            String question = scanner.nextLine().trim();

            // Check if user wants to exit
            if (question.equalsIgnoreCase("exit") || question.isBlank()) {
                System.out.println("Exiting the application. Goodbye!");
                break;
            }

            try {
                // Query the RAG system and display the response
                System.out.println("\nThinking...");
                String response = ragService.query(question);
                System.out.println("\nResponse:");
                System.out.println(response);
            } catch (Exception e) {
                System.err.println("Error processing your question: " + e.getMessage());
            }
        }

        // Close the scanner
        scanner.close();

        // Exit the application
        System.exit(0);
    }
}