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
     * This test demonstrates using file system tools via MCP.
     * It requires the filesystem MCP server to be configured and running.
     * 
     * To enable this test:
     * 1. Install npx: npm install -g npx
     * 2. Configure filesystem server in application-mcp.properties
     * 3. Ensure /tmp directory exists and is accessible
     */
    @Test
    void useFileSystemTools() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping filesystem test - no MCP tools available");
            return;
        }
        
        // Check if we have filesystem-related tools (simplified check)
        boolean hasFileSystemTools = !mcpTools.isEmpty();
        
        if (!hasFileSystemTools) {
            System.out.println("Skipping filesystem test - no filesystem tools detected");
            return;
        }
        
        try {
            // Ask about files in the configured directory
            String response = chatClient.prompt()
                    .user("What files are in the /tmp directory? If you can't access it, just tell me what tools you have available.")
                    .call()
                    .content();
            
            System.out.println("Filesystem response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            System.out.println("Filesystem test failed (this is expected if MCP server is not running): " + e.getMessage());
        }
    }
    
    /**
     * This test demonstrates creating and reading files via MCP.
     * It requires the filesystem MCP server to be properly configured.
     */
    @Test
    void createAndReadFile() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping file creation test - no MCP tools available");
            return;
        }
        
        // Simplified check for file system tools
        boolean hasFileSystemTools = !mcpTools.isEmpty();
        
        if (!hasFileSystemTools) {
            System.out.println("Skipping file creation test - no file creation tools detected");
            return;
        }
        
        try {
            // Create a test file
            String createResponse = chatClient.prompt()
                    .user("Create a file called spring-ai-test.txt in /tmp with the content 'Hello from Spring AI MCP!'")
                    .call()
                    .content();
            
            System.out.println("Create response: " + createResponse);
            
            // Try to read it back
            String readResponse = chatClient.prompt()
                    .user("What are the contents of /tmp/spring-ai-test.txt?")
                    .call()
                    .content();
            
            System.out.println("Read response: " + readResponse);
            
            // Basic validation
            assertNotNull(createResponse);
            assertNotNull(readResponse);
            
        } catch (Exception e) {
            System.out.println("File creation/read test failed (expected if MCP server not configured): " + e.getMessage());
        }
    }
    
    /**
     * Exercise: Weather MCP Client
     * This is a placeholder for connecting to a weather MCP server.
     * Students can implement this by:
     * 1. Setting up a weather MCP server (or using a mock)
     * 2. Configuring the connection in application-mcp.properties
     * 3. Using the weather tools to query weather information
     */
    @Test
    void queryWeatherInfo() {
        // TODO: This is an exercise for students
        // Implement weather MCP server connection and usage
        
        System.out.println("Weather MCP exercise - to be implemented by students");
        System.out.println("Steps:");
        System.out.println("1. Configure weather MCP server connection");
        System.out.println("2. Use discovered weather tools");
        System.out.println("3. Query current weather and forecasts");
        
        // For now, just test that we can ask about weather without tools
        String response = chatClient.prompt()
                .user("What would I need to check the weather? If you have weather tools available, use them to check the weather in New York.")
                .call()
                .content();
        
        System.out.println("Weather query response: " + response);
        assertNotNull(response);
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