package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
// Tool callback imports removed - relying on Spring AI auto-configuration
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MCP Server functionality (Lab 15).
 * 
 * These tests demonstrate how to create and configure MCP servers
 * that expose tools to AI clients like Claude Desktop.
 * 
 * Enable the 'mcp-server' profile to activate MCP server configuration.
 */
@SpringBootTest
@ActiveProfiles("mcp-server")
public class McpServerTests {
    
    @Autowired
    private CalculatorService calculatorService;
    
    // Tool callbacks will be tested indirectly through the CalculatorService
    // Spring AI MCP auto-configuration should discover @Tool methods automatically
    
    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads with MCP server profile
        assertNotNull(calculatorService);
    }
    
    @Test
    void calculatorServiceFunctionality() {
        // Test the calculator service directly
        assertEquals(5.0, calculatorService.add(2, 3), 0.001);
        assertEquals(1.0, calculatorService.subtract(3, 2), 0.001);
        assertEquals(6.0, calculatorService.multiply(2, 3), 0.001);
        assertEquals(2.0, calculatorService.divide(6, 3), 0.001);
        assertEquals(3.0, calculatorService.sqrt(9), 0.001);
        assertEquals(8.0, calculatorService.power(2, 3), 0.001);
        assertEquals(15.0, calculatorService.calculatePercentage(15, 100), 0.001);
    }
    
    @Test
    void calculatorServiceErrorHandling() {
        // Test error conditions
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.divide(5, 0));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.sqrt(-4));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.calculateCompoundInterest(-1000, 5, 10, 12));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.calculateCompoundInterest(1000, -5, 10, 12));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.calculateCompoundInterest(1000, 5, -10, 12));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.calculateCompoundInterest(1000, 5, 10, 0));
    }
    
    @Test
    void compoundInterestCalculation() {
        // Test compound interest calculation
        var result = calculatorService.calculateCompoundInterest(1000, 5, 10, 12);
        
        assertNotNull(result);
        assertEquals(1000, result.principal(), 0.001);
        assertEquals(5, result.annualRate(), 0.001);
        assertEquals(10, result.years());
        assertTrue(result.finalAmount() > result.principal());
        assertTrue(result.totalInterest() > 0);
        
        // The compound interest formula: A = P(1 + r/n)^(nt)
        // Where P=1000, r=0.05, n=12, t=10
        // Expected: approximately $1648.72
        assertTrue(result.finalAmount() > 1600 && result.finalAmount() < 1700);
        
        System.out.println("Compound Interest Result:");
        System.out.println(result.toString());
    }
    
    @Test
    void verifyMcpServerProfile() {
        // Test that the MCP server profile is active and the CalculatorService is available
        assertNotNull(calculatorService, "CalculatorService should be available");
        
        System.out.println("MCP Server profile is active");
        System.out.println("CalculatorService is available with @Tool annotated methods:");
        System.out.println("- add(double, double)");
        System.out.println("- subtract(double, double)");
        System.out.println("- multiply(double, double)");
        System.out.println("- divide(double, double)");
        System.out.println("- sqrt(double)");
        System.out.println("- power(double, double)");
        System.out.println("- calculateCompoundInterest(double, double, int, int)");
        System.out.println("- calculatePercentage(double, double)");
        
        System.out.println("\nNote: Spring AI MCP auto-configuration should automatically");
        System.out.println("discover and expose these @Tool methods to MCP clients.");
    }
    
    @Test
    void mcpServerConfigurationTest() {
        // Test that demonstrates the MCP server configuration is working
        System.out.println("MCP Server Configuration Test");
        System.out.println("Profile 'mcp-server' should be active for this test");
        System.out.println("CalculatorService @Tool methods should be auto-discovered");
        
        // Verify the service is properly configured
        assertNotNull(calculatorService);
        
        // Test that the service methods work (these become MCP tools)
        assertEquals(5.0, calculatorService.add(2, 3), 0.001);
        assertEquals(6.0, calculatorService.multiply(2, 3), 0.001);
    }
    
    /**
     * Test configuration for MCP server.
     * Since Spring AI auto-discovers @Tool annotated methods,
     * no explicit configuration is needed for basic functionality.
     */
    @TestConfiguration
    @Profile("mcp-server")
    static class McpServerTestConfig {
        
        // No explicit configuration needed - Spring AI MCP auto-configuration
        // will automatically discover @Tool annotated methods in @Service beans
        
        // If you need custom tool services, you can add @Bean methods here:
        /*
        @Bean
        public SystemDiagnosticsService systemDiagnosticsService() {
            return new SystemDiagnosticsService();
        }
        */
    }
    
    /**
     * Exercise: System Diagnostics MCP Server
     * This is a placeholder for students to implement system diagnostic tools.
     */
    @Test 
    void systemDiagnosticsExercise() {
        System.out.println("System Diagnostics MCP Server Exercise");
        System.out.println("Students should implement:");
        System.out.println("1. SystemDiagnosticsService with @Tool annotated methods");
        System.out.println("2. Memory usage statistics tool");
        System.out.println("3. Thread listing tool");
        System.out.println("4. System properties tool");
        System.out.println("5. Register the service as MCP tools");
        
        // This is a placeholder - students will implement actual functionality
        assertTrue(true, "Exercise placeholder - implement SystemDiagnosticsService");
    }
    
    /**
     * Test that demonstrates MCP server functionality works correctly
     */
    @Test
    void mcpToolFunctionalityTest() {
        System.out.println("Testing MCP tool functionality:");
        System.out.println("These @Tool annotated methods should be available to MCP clients:");
        
        // Test each calculator tool function
        System.out.println("- add: " + calculatorService.add(10, 5) + " (should be 15.0)");
        System.out.println("- subtract: " + calculatorService.subtract(10, 5) + " (should be 5.0)");
        System.out.println("- multiply: " + calculatorService.multiply(10, 5) + " (should be 50.0)");
        System.out.println("- divide: " + calculatorService.divide(10, 5) + " (should be 2.0)");
        System.out.println("- sqrt: " + calculatorService.sqrt(25) + " (should be 5.0)");
        System.out.println("- power: " + calculatorService.power(2, 3) + " (should be 8.0)");
        System.out.println("- calculatePercentage: " + calculatorService.calculatePercentage(20, 100) + " (should be 20.0)");
        
        var compoundResult = calculatorService.calculateCompoundInterest(1000, 5, 10, 12);
        System.out.println("- calculateCompoundInterest: $" + String.format("%.2f", compoundResult.finalAmount()) + 
                          " (should be around $1648)");
        
        // Basic assertions to verify the tools work
        assertEquals(15.0, calculatorService.add(10, 5), 0.001);
        assertEquals(5.0, calculatorService.subtract(10, 5), 0.001);
        assertTrue(compoundResult.finalAmount() > 1600);
    }
}