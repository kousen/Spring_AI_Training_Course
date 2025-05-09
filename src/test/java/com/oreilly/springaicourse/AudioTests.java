package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.speech.AudioTranscriptionPrompt;
import org.springframework.ai.speech.SpeechPrompt;
import org.springframework.ai.speech.SpeechResponse;
import org.springframework.ai.speech.TranscriptionResponse;
import org.springframework.ai.speech.api.TranscriptResponseFormat;
import org.springframework.ai.speech.api.Voice;
import org.springframework.ai.speech.api.AudioResponseFormat;
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
@ActiveProfiles("test") // Optional, if you have a test profile
class AudioTests {

    @Autowired
    private OpenAiChatModel model;
    
    @Autowired
    private ChatMemory memory;
    
    private ChatClient chatClient;
    
    // For audio transcription testing
    // Note: You would need to add a sample MP3 file to your resources
    @Value("classpath:audio/sample.mp3")
    private Resource sampleAudioResource;
    
    @BeforeEach
    void setUp() {
        chatClient = ChatClient.create(model);
    }
    
    @Test
    void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
        String text = "Welcome to Spring AI, a powerful framework for integrating AI into your Spring applications.";
        
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .withVoice(Voice.ALLOY) // Options: ALLOY, ECHO, FABLE, ONYX, NOVA, SHIMMER
                .withResponseFormat(AudioResponseFormat.MP3)
                .withSpeed(1.0f)
                .build();
        
        SpeechPrompt prompt = new SpeechPrompt(text, options);
        SpeechResponse response = speechModel.call(prompt);
        assertNotNull(response);
        
        // Optionally save to file for verification
        try {
            Files.write(Path.of("generated_audio.mp3"), response.getResult().getOutput());
            System.out.println("Audio file generated and saved as 'generated_audio.mp3'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void speechToText(@Autowired OpenAiAudioTranscriptionModel transcriptionModel) {
        try {
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(sampleAudioResource.getInputStream());
            
            // Optional configuration
            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .withLanguage("en")
                    .withPrompt("Transcribe this audio file.")
                    .withTemperature(0.0f)
                    .withResponseFormat(TranscriptResponseFormat.TEXT)
                    .build();
            
            prompt.setOptions(options);
            
            TranscriptionResponse response = transcriptionModel.call(prompt);
            assertNotNull(response);
            System.out.println("Transcription: " + response.getResult().getOutput());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}