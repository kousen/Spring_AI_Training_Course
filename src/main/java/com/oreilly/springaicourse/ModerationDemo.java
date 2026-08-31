package com.oreilly.springaicourse;

import org.springframework.ai.moderation.*;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("DataFlowIssue")
@Component
public class ModerationDemo {
    private final OpenAiModerationModel openAiModerationModel;

    @Autowired
    public ModerationDemo(OpenAiModerationModel openAiModerationModel) {
        this.openAiModerationModel = openAiModerationModel;
    }

    public void demo() {
        OpenAiModerationOptions moderationOptions = OpenAiModerationOptions.builder()
                .model("omni-moderation-latest")
                .build();

        ModerationPrompt moderationPrompt = new ModerationPrompt("Text to be moderated", moderationOptions);
        ModerationResponse response = openAiModerationModel.call(moderationPrompt);

// Access the moderation results
        Moderation moderation = response.getResult().getOutput();

// Print general information
        System.out.println("Moderation ID: " + moderation.getId());
        System.out.println("Model used: " + moderation.getModel());

// Access the moderation results (there's usually only one, but it's a list)
        for (ModerationResult result : moderation.getResults()) {
            System.out.println("\nModeration Result:");
            System.out.println("Flagged: " + result.isFlagged());

            // Access categories
            Categories categories = result.getCategories();
            System.out.println("\nCategories:");
            assert categories != null;
            System.out.println("Sexual: " + categories.isSexual());
            System.out.println("Hate: " + categories.isHate());
            System.out.println("Harassment: " + categories.isHarassment());
            System.out.println("Self-Harm: " + categories.isSelfHarm());
            System.out.println("Sexual/Minors: " + categories.isSexualMinors());
            System.out.println("Hate/Threatening: " + categories.isHateThreatening());
            System.out.println("Violence/Graphic: " + categories.isViolenceGraphic());
            System.out.println("Self-Harm/Intent: " + categories.isSelfHarmIntent());
            System.out.println("Self-Harm/Instructions: " + categories.isSelfHarmInstructions());
            System.out.println("Harassment/Threatening: " + categories.isHarassmentThreatening());
            System.out.println("Violence: " + categories.isViolence());

            // Access category scores
            CategoryScores scores = result.getCategoryScores();
            assert scores != null;
            System.out.println("\nCategory Scores:");
            System.out.println("Sexual: " + scores.getSexual());
            System.out.println("Hate: " + scores.getHate());
            System.out.println("Harassment: " + scores.getHarassment());
            System.out.println("Self-Harm: " + scores.getSelfHarm());
            System.out.println("Sexual/Minors: " + scores.getSexualMinors());
            System.out.println("Hate/Threatening: " + scores.getHateThreatening());
            System.out.println("Violence/Graphic: " + scores.getViolenceGraphic());
            System.out.println("Self-Harm/Intent: " + scores.getSelfHarmIntent());
            System.out.println("Self-Harm/Instructions: " + scores.getSelfHarmInstructions());
            System.out.println("Harassment/Threatening: " + scores.getHarassmentThreatening());
            System.out.println("Violence: " + scores.getViolence());
        }
    }
}
