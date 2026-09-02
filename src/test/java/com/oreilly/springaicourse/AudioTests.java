package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import com.openai.models.audio.AudioResponseFormat;
import org.springframework.ai.openai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
//@EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
class AudioTests {

    // For audio transcription testing
    @Value("classpath:audio/tftjs.mp3")
    private Resource sampleAudioResource;

    @Test
    void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
        String text = "Welcome to Spring AI, a powerful framework for integrating AI into your Spring applications.";
        
        var options = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioSpeechOptions.Voice.ALLOY)
                .responseFormat(OpenAiAudioSpeechOptions.AudioResponseFormat.MP3)
                .speed(1.0)
                .build();
        
        var prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse response = speechModel.call(prompt);
        assertNotNull(response);
        
        // Optionally save to file for verification
        try {
            Path outputPath = Path.of("build", "generated-audio", "generated_audio.mp3");
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, response.getResult().getOutput());
            System.out.println("Audio file generated and saved as " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void speechToText(@Autowired OpenAiAudioTranscriptionModel transcriptionModel) {
        // Optional configuration
        var options = OpenAiAudioTranscriptionOptions.builder()
                .language("en")
                .prompt("Transcribe this audio file.")
                .temperature(0.0f)
                .responseFormat(AudioResponseFormat.TEXT)
                .build();

        var prompt = new AudioTranscriptionPrompt(sampleAudioResource, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        assertNotNull(response);
        System.out.println("Transcription: " + response.getResult().getOutput());
    }
}
