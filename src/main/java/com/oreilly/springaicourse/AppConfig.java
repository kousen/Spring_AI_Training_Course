package com.oreilly.springaicourse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfig {
    private final TextSplitter splitter = TokenTextSplitter.builder().build();

    @Value("classpath:/rag/spring-ai-course.md")
    private Resource springAiCourseNotes;

    @Value("classpath:/rag/future-of-jobs-summary.md")
    private Resource futureOfJobsSummary;

    @Value("classpath:/rag/kendrick-drake-summary.md")
    private Resource kendrickDrakeSummary;

    @Bean
    @Profile("rag")
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> {
            System.out.println("Using vector store: " + vectorStore.getClass().getSimpleName());

            // Check if we're using Redis and if data already exists
            boolean isRedisStore = vectorStore.getClass().getSimpleName().toLowerCase().contains("redis");
            boolean dataExists;

            System.out.println("Using vector store class: " + vectorStore.getClass().getName());
            System.out.println("Redis detection enabled: " + isRedisStore);

            if (isRedisStore) {
                // Sample query to check if data exists by looking for existing Spring Framework content
                try {
                    // Simple approach: search for something we know should be there
                    System.out.println("Checking if data exists by searching for 'Spring Framework'...");
                    var results = vectorStore.similaritySearch("Spring Framework");
                    dataExists = !results.isEmpty();  // This is the actual check
                    System.out.println("Search returned " + results.size() + " results");

                    if (dataExists) {
                        System.out.println("Data already exists in Redis vector store - skipping data loading");
                        return;
                    }
                } catch (Exception e) {
                    // If the search fails, it likely means the data doesn't exist yet
                    System.out.println("No existing data found in Redis vector store");
                }
            }

            System.out.println("Loading course-local RAG documents");

            List<Document> documents = List.of(
                    documentFrom(springAiCourseNotes, "spring_ai_course"),
                    documentFrom(futureOfJobsSummary, "wef_jobs_report_summary"),
                    documentFrom(kendrickDrakeSummary, "kendrick_drake_summary")
            );

            List<Document> chunks = splitter.apply(documents);
            System.out.println("Split into " + chunks.size() + " chunks");

            vectorStore.add(chunks);
            System.out.println("RAG corpus loaded");
        };
    }

    private Document documentFrom(Resource resource, String source) throws IOException {
        return new Document(
                resource.getContentAsString(StandardCharsets.UTF_8),
                Map.of("source", source, "filename", resource.getFilename())
        );
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
