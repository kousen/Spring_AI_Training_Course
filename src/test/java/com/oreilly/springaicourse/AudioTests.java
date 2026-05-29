package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
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
@EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
class AudioTests {

    // For audio transcription testing
    @Value("classpath:audio/tftjs.mp3")
    private Resource sampleAudioResource;

    // === Lab 9: Audio Processing ===

    @Test
    void testTextToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) throws IOException {
        // TODO: Implement text-to-speech conversion
        // 1. Create a TextToSpeechPrompt with text and optional OpenAiAudioSpeechOptions
        // 2. Use speechModel.call() to generate audio
        // 3. Save the result to an MP3 file in build/generated-audio/
        // 4. Print confirmation message
    }

    @Test
    void testSpeechToText(@Autowired OpenAiAudioTranscriptionModel transcriptionModel) {
        // TODO: Implement speech-to-text transcription
        // 1. Create AudioTranscriptionPrompt with the sample audio resource
        // 2. Use transcriptionModel.call() to transcribe audio
        // 3. Print the transcribed text
        // 4. Assert the result is not null
    }

}
