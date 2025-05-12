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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

            // Process URLs
            List.of(FEUD_URL, SPRING_URL).forEach(url -> {
                // Fetch HTML content using Jsoup
                List<Document> documents = new JsoupDocumentReader(url).get();
                System.out.println("Fetched " + documents.size() + " documents from " + url);

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