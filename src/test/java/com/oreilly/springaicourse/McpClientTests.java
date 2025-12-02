package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MCP Client functionality (Lab 14).
 *
 * These tests demonstrate how to use Spring AI's MCP client to connect
 * to external MCP servers and use their tools.
 *
 * This lab uses:
 * - Context7: For library documentation lookup (https://github.com/upstash/context7)
 * - Tavily: For AI-optimized web search (requires TAVILY_API_KEY)
 *
 * Note: Some tests require external MCP servers to be running or available.
 * Enable the 'mcp' profile to activate MCP client configuration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("mcp")
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class McpClientTests {

    @Autowired
    private ChatModel chatModel;

    @Autowired(required = false)
    private List<ToolCallback> mcpTools;  // Auto-discovered MCP tools

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        // Create a chat client with the specified model and MCP tools if available
        if (mcpTools != null && !mcpTools.isEmpty()) {
            chatClient = ChatClient.builder(chatModel)
                    .defaultToolCallbacks(mcpTools)
                    .build();
        } else {
            chatClient = ChatClient.builder(chatModel).build();
        }
    }

    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads with MCP profile
        assertNotNull(chatModel);
        assertNotNull(chatClient);
    }

    @Test
    void listAvailableTools() {
        if (mcpTools != null) {
            System.out.println("Available MCP tools: " + mcpTools.size());
            mcpTools.forEach(tool -> {
                System.out.println("- Tool callback available: " + tool.getClass().getSimpleName());
            });

            assertFalse(mcpTools.isEmpty(), "Should have discovered MCP tools when servers are configured");
        } else {
            System.out.println("No MCP tools discovered. This is expected if no MCP servers are configured.");
        }
    }

    @Test
    void basicChatWithoutTools() {
        // Test basic chat functionality even without MCP tools
        String response = chatClient.prompt()
                .user("What is 2 + 2?")
                .call()
                .content();

        System.out.println("Basic math response: " + response);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.toLowerCase().contains("4") || response.toLowerCase().contains("four"));
    }

    /**
     * This test demonstrates using Context7 to look up library documentation.
     * Context7 provides up-to-date documentation for libraries and frameworks.
     *
     * To enable this test:
     * 1. Install npx: npm install -g npx
     * 2. Configure Context7 server in application-mcp.properties (enabled by default)
     */
    @Test
    void lookupLibraryDocumentation() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping Context7 test - no MCP tools available");
            return;
        }

        try {
            // Use Context7 to look up Spring AI documentation
            String response = chatClient.prompt()
                    .user("Using Context7, find documentation about ChatClient in Spring AI. " +
                          "What are the main methods available?")
                    .call()
                    .content();

            System.out.println("Context7 documentation response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            System.out.println("Context7 test failed: " + e.getMessage());
        }
    }

    /**
     * This test demonstrates getting code examples from documentation via Context7.
     */
    @Test
    void getFrameworkExamples() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping Context7 example test - no MCP tools available");
            return;
        }

        try {
            // Ask for code examples from documentation
            String response = chatClient.prompt()
                    .user("Using Context7, show me examples of how to use prompt templates " +
                          "in Spring AI. Include any code snippets from the documentation.")
                    .call()
                    .content();

            System.out.println("Context7 examples response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            System.out.println("Context7 examples test failed: " + e.getMessage());
        }
    }

    /**
     * This test demonstrates combining Context7 and Tavily for comprehensive research.
     * Requires both servers to be configured and TAVILY_API_KEY to be set.
     */
    @Test
    void combineDocumentationAndWebSearch() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping combined test - no MCP tools available");
            return;
        }

        try {
            // First, get documentation context
            String docsResponse = chatClient.prompt()
                    .user("Using Context7, what is the recommended way to implement " +
                          "RAG (Retrieval-Augmented Generation) in Spring AI?")
                    .call()
                    .content();

            System.out.println("Documentation response: " + docsResponse);

            // Then, search for recent community discussions or updates
            // This requires TAVILY_API_KEY to be set and Tavily server configured
            String searchResponse = chatClient.prompt()
                    .user("Using Tavily, search for recent blog posts or tutorials about " +
                          "Spring AI RAG implementation best practices from 2024-2025.")
                    .call()
                    .content();

            System.out.println("Web search response: " + searchResponse);

            assertNotNull(docsResponse);
            assertNotNull(searchResponse);
        } catch (Exception e) {
            System.out.println("Combined test failed: " + e.getMessage());
        }
    }

    /**
     * Research Assistant - Complete implementation
     * Combines Context7 and Tavily to build a research assistant that can answer
     * questions using both official documentation and current web content.
     */
    @Test
    void researchAssistant() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping research assistant test - no MCP tools available");
            // Still test basic functionality
            String response = chatClient.prompt()
                    .user("What tools do you have available? List them and describe what each one does.")
                    .call()
                    .content();
            System.out.println("Tools query response: " + response);
            assertNotNull(response);
            return;
        }

        try {
            // Research question about Spring AI tools/function calling
            String question = "How do I implement function calling (tools) in Spring AI, " +
                              "and what are some real-world use cases?";

            System.out.println("Research question: " + question);

            // Step 1: Get official documentation via Context7
            String docsResponse = chatClient.prompt()
                    .user("Using Context7, find documentation about implementing tools " +
                          "(function calling) in Spring AI. Include the @Tool annotation usage " +
                          "and how to register tools with ChatClient.")
                    .call()
                    .content();

            System.out.println("\n=== Official Documentation (Context7) ===");
            System.out.println(docsResponse);

            // Step 2: Search for real-world examples via Tavily
            String webResponse = chatClient.prompt()
                    .user("Using Tavily, search for real-world examples and tutorials about " +
                          "Spring AI function calling and tools. Look for blog posts, GitHub " +
                          "examples, or community discussions from 2024-2025.")
                    .call()
                    .content();

            System.out.println("\n=== Real-World Examples (Tavily) ===");
            System.out.println(webResponse);

            // Step 3: Synthesize the results
            String synthesizedAnswer = chatClient.prompt()
                    .user("Based on the documentation and examples you found, provide a " +
                          "comprehensive answer to: " + question + "\n\n" +
                          "Include: 1) Basic implementation steps, 2) Best practices, " +
                          "3) Real-world use cases, 4) Common pitfalls to avoid.")
                    .call()
                    .content();

            System.out.println("\n=== Synthesized Answer ===");
            System.out.println(synthesizedAnswer);

            assertNotNull(docsResponse);
            assertNotNull(webResponse);
            assertNotNull(synthesizedAnswer);

        } catch (Exception e) {
            System.out.println("Research assistant test failed: " + e.getMessage());
        }
    }

    /**
     * Test to demonstrate multiple MCP servers working together
     */
    @Test
    void multipleServerIntegration() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping multiple server test - no MCP tools available");
            return;
        }

        System.out.println("Testing integration with multiple MCP servers:");
        System.out.println("Available tools from all connected servers:");

        mcpTools.forEach(tool -> {
            System.out.println("  - Tool callback: " + tool.getClass().getSimpleName());
        });

        // Test a complex query that might use multiple tools
        String complexResponse = chatClient.prompt()
                .user("Help me understand what tools you have available and demonstrate using one of them.")
                .call()
                .content();

        System.out.println("Complex query response: " + complexResponse);
        assertNotNull(complexResponse);
        assertFalse(complexResponse.isEmpty());
    }
}
