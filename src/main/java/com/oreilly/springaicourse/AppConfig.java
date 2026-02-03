package com.oreilly.springaicourse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfig {
    private static final String FEUD_URL = "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud";
    private static final String SPRING_URL = "https://en.wikipedia.org/wiki/Spring_Framework";

    private final TextSplitter splitter = new TokenTextSplitter();

    @Value("classpath:/pdfs/WEF_Future_of_Jobs_Report_2025.pdf")
    private Resource jobsReport2025;

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

            System.out.println("Loading data into vector store");

            // Process URLs
            List.of(FEUD_URL, SPRING_URL).forEach(url -> {
                // Fetch HTML content using Jsoup directly (with User-Agent to avoid 403 from Wikipedia)
                List<Document> documents = fetchHtmlDocuments(url);
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
                var pdfReader = new PagePdfDocumentReader(jobsReport2025);

                List<Document> pdfDocuments = pdfReader.get();
                System.out.printf("Fetched %d documents from %s%n", pdfDocuments.size(), jobsReport2025.getFilename());

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

    /**
     * Fetches HTML from a URL using Jsoup's connect API, which sets a proper User-Agent.
     * This avoids 403 errors from sites like Wikipedia that block bare HttpURLConnection requests.
     */
    private List<Document> fetchHtmlDocuments(String url) {
        try {
            String html = Jsoup.connect(url).get().html();
            InputStream inputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            Resource resource = new InputStreamResource(inputStream);
            return new JsoupDocumentReader(resource).get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch HTML from: " + url, e);
        }
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}