package com.oreilly.springaicourse;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AppConfig {
    private static final String FEUD_URL = "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud";
    private static final String SPRING_URL = "https://en.wikipedia.org/wiki/Spring_Framework";

    private final TextSplitter splitter = new TokenTextSplitter();
    private final RedisTemplate<String, String> redisTemplate;

    @Value("classpath:/pdfs/WEF_Future_of_Jobs_Report_2025.pdf")
    private Resource jobsReport2025;

    public AppConfig(@Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    @Profile("rag")
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> {
            System.out.println("Using vector store: " + vectorStore.getClass().getSimpleName());

            // Check if we're using Redis and if data already exists
            boolean isRedisStore = vectorStore.getClass().getSimpleName().toLowerCase().contains("redis");
            boolean dataExists = false;

            System.out.println("\n###################################################");
            System.out.println("Using vector store class: " + vectorStore.getClass().getName());
            System.out.println("Redis detection enabled: " + isRedisStore);
            System.out.println("###################################################\n");

            if (isRedisStore && redisTemplate != null) {
                // Sample query to check if data exists by looking for existing Spring Framework content
                try {
                    // Simple approach: just search for something we know should be there
                    System.out.println("Checking if data exists by searching for 'Spring Framework'...");
                    var results = vectorStore.similaritySearch("Spring Framework");
                    dataExists = !results.isEmpty();
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

            System.out.println("Loading data into vector store");

            // Process URLs
            List.of(FEUD_URL, SPRING_URL).forEach(url -> {
                // Fetch HTML content using Jsoup
                List<Document> documents = new JsoupDocumentReader(url).get();
                System.out.println("Fetched " + documents.size() + " documents from " + url);

                // Add source metadata to help identify content later
                documents.forEach(doc -> {
                    String source = url.contains("Drake") ? "drake_feud" : "spring_framework";
                    doc.getMetadata().put("source", source);
                });

                // Split the document into chunks
                List<Document> chunks = splitter.apply(documents);
                System.out.println("Split into " + chunks.size() + " chunks");

                // Add the chunks to the vector store
                vectorStore.add(chunks);
            });

            try {
                // Add PDF to the vector store
                System.out.println("Processing PDF document (this may take a few minutes)...");

                // Process a specific page range for better performance
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(jobsReport2025);

                List<Document> pdfDocuments = pdfReader.get();
                System.out.println("Fetched " + pdfDocuments.size() + " documents from " + jobsReport2025.getFilename());

                // Add source metadata to help identify PDF content
                pdfDocuments.forEach(doc -> {
                    doc.getMetadata().put("source", "wef_jobs_report");
                    doc.getMetadata().put("type", "pdf");
                });

                List<Document> pdfChunks = splitter.apply(pdfDocuments);
                System.out.println("Split into " + pdfChunks.size() + " chunks");

                vectorStore.add(pdfChunks);
                System.out.println("PDF processing complete!");
            } catch (Exception e) {
                System.err.println("Error processing PDF: " + e.getMessage());
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}