package com.oreilly.springaicourse;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {
    
    @Tool(description = "Add two numbers together")
    public double add(double a, double b) {
        return a + b;
    }
    
    @Tool(description = "Subtract the second number from the first")
    public double subtract(double a, double b) {
        return a - b;
    }
    
    @Tool(description = "Multiply two numbers")
    public double multiply(double a, double b) {
        return a * b;
    }
    
    @Tool(description = "Divide the first number by the second")
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return a / b;
    }
    
    @Tool(description = "Calculate the square root of a number")
    public double sqrt(double number) {
        if (number < 0) {
            throw new IllegalArgumentException("Cannot calculate square root of negative number");
        }
        return Math.sqrt(number);
    }
    
    @Tool(description = "Calculate a number raised to a power")
    public double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }
    
    @Tool(description = "Calculate compound interest given principal, annual rate (as percentage), years, and compounding frequency per year")
    public CompoundInterestResult calculateCompoundInterest(
            double principal,
            double annualRate,
            int years,
            int compoundingFrequency) {
        
        if (principal <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (annualRate < 0) {
            throw new IllegalArgumentException("Annual rate cannot be negative");
        }
        if (years <= 0) {
            throw new IllegalArgumentException("Years must be positive");
        }
        if (compoundingFrequency <= 0) {
            throw new IllegalArgumentException("Compounding frequency must be positive");
        }
        
        double rate = annualRate / 100;
        double amount = principal * Math.pow(1 + rate / compoundingFrequency, 
                                           compoundingFrequency * years);
        double interest = amount - principal;
        
        return new CompoundInterestResult(principal, amount, interest, years, annualRate);
    }
    
    @Tool(description = "Calculate what percentage one number is of another (e.g., percentage=15, number=100 returns 15)")
    public double calculatePercentage(double percentage, double number) {
        return (percentage / 100) * number;
    }
    
    /**
     * Result record for compound interest calculations
     */
    public record CompoundInterestResult(
            double principal,
            double finalAmount,
            double totalInterest,
            int years,
            double annualRate
    ) {
        @Override
        public String toString() {
            return String.format(
                    """
                            Compound Interest Calculation:
                            Principal: $%.2f
                            Annual Rate: %.2f%%
                            Years: %d
                            Final Amount: $%.2f
                            Total Interest: $%.2f""",
                principal, annualRate, years, finalAmount, totalInterest
            );
        }
    }
}