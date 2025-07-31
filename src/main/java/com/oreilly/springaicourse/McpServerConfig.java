package com.oreilly.springaicourse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;

/**
 * Configuration class for MCP Server functionality (Lab 15).
 * 
 * This configuration is activated when the 'mcp-server' profile is enabled.
 * The CalculatorService @Tool annotated methods are automatically discovered
 * by Spring AI's MCP server auto-configuration.
 * 
 * This configuration class demonstrates:
 * 1. How to add startup logging for MCP server
 * 2. Where to add additional tool services
 * 3. Server capability documentation
 */
@Configuration
@Profile("mcp-server")
public class McpServerConfig {
    
    /**
     * Application runner that logs MCP server startup information.
     * This helps developers understand what tools are being exposed.
     */
    @Bean
    public ApplicationRunner mcpServerStartupLogger() {
        return args -> {
            System.out.println("\n=== MCP Server Started ===");
            System.out.println("Profile: mcp-server");
            System.out.println("Available tools from CalculatorService:");
            System.out.println("  • add(double, double) - Add two numbers");
            System.out.println("  • subtract(double, double) - Subtract numbers");
            System.out.println("  • multiply(double, double) - Multiply numbers");
            System.out.println("  • divide(double, double) - Divide numbers");
            System.out.println("  • sqrt(double) - Square root");
            System.out.println("  • power(double, double) - Power calculation");
            System.out.println("  • calculateCompoundInterest(...) - Compound interest");
            System.out.println("  • calculatePercentage(double, double) - Percentage");
            System.out.println("\nConnect to this server using:");
            System.out.println("  • Claude Desktop MCP configuration");
            System.out.println("  • STDIO transport mode");
            System.out.println("  • SSE transport mode (uncomment config in properties)");
            System.out.println("========================\n");
        };
    }
    
    // Example: Students can add custom tool services here
    /*
    @Bean
    public SystemDiagnosticsService systemDiagnosticsService() {
        return new SystemDiagnosticsService();
    }
    
    @Bean 
    public FileOperationsService fileOperationsService() {
        return new FileOperationsService();
    }
    */
    
    // The CalculatorService with @Tool annotated methods is automatically
    // discovered by Spring AI's MCP server auto-configuration.
    // No explicit tool registration is required.
}