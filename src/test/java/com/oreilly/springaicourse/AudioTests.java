package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
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
class AudioTests {

    // For audio transcription testing
    @Value("classpath:audio/tftjs.mp3")
    private Resource sampleAudioResource;

    @Test
    void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
        String text = "Welcome to Spring AI, a powerful framework for integrating AI into your Spring applications.";
        
        var options = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .build();
        
        var prompt = new SpeechPrompt(text, options);
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
        // Optional configuration
        var options = OpenAiAudioTranscriptionOptions.builder()
                .language("en")
                .prompt("Transcribe this audio file.")
                .temperature(0.0f)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();

        var prompt = new AudioTranscriptionPrompt(sampleAudioResource, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        assertNotNull(response);
        System.out.println("Transcription: " + response.getResult().getOutput());
    }
}