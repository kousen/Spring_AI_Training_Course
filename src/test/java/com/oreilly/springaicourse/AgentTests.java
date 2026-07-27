package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Capstone (Lab 16): an "agent" is not a new API -- it is the pieces you have
 * already built, composed: a model in a loop (ToolCallingAdvisor, auto-registered
 * by ChatClient in Spring AI 2.0), tools it can call, and memory across turns.
 *
 * The model plans the tool sequence itself: nothing in this test tells it to
 * call the date tool before the calculator.
 */
@SuppressWarnings("unused")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class AgentTests {

    @Autowired
    private ChatModel model;   // primary model (OpenAI)

    @Autowired
    private ChatMemory memory;

    @Test
    void multiStepPlanningWithMemory() {
        String conversationId = "college-fund";
        ChatClient agent = ChatClient.builder(model)
                .defaultTools(new DateTimeTools(), new CalculatorService())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        // Step 1: a task that requires chaining tools the model chooses itself
        // (current date -> compound interest calculation)
        String plan = agent.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user("""
                        I'm investing $10,000 today at 5 percent annual interest,
                        compounded monthly. What will it be worth 10 years from
                        today? Use your tools for the date and the calculation,
                        and state the final amount and target date.""")
                .call()
                .content();
        System.out.println(plan);
        assertNotNull(plan);
        // 10k at 5% compounded monthly for 10 years ~= $16,470
        assertTrue(plan.contains("16,4") || plan.contains("16.4") || plan.contains("164"),
                "Expected ~$16,470 in: " + plan);

        // Step 2: memory makes it conversational -- no restating the problem
        String followUp = agent.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user("How much of that final amount is interest rather than principal?")
                .call()
                .content();
        System.out.println(followUp);
        assertNotNull(followUp);
        assertTrue(followUp.contains("6,4") || followUp.contains("6.4") || followUp.contains("64"),
                "Expected ~$6,470 of interest in: " + followUp);
    }
}
