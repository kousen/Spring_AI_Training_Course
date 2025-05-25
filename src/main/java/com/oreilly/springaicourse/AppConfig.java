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

            // TODO: Implement document loading and processing
            // 1. Check if using Redis and if data already exists (for efficiency)
            // 2. Load documents from various sources:
            //    - PDF documents using PagePdfDocumentReader
            //    - Web pages using JsoupDocumentReader  
            // 3. Split documents using TextSplitter
            // 4. Add processed documents to vector store
            
            System.out.println("TODO: Document loading and vector store population not yet implemented");
            System.out.println("Available sources: " + SPRING_URL + ", " + FEUD_URL + ", " + jobsReport2025.getFilename());
        };
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        // TODO: Create and configure SimpleVectorStore
        // This is the default in-memory vector store
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // TODO: Add Redis VectorStore configuration
    // @Bean
    // @Profile("redis") 
    // VectorStore redisVectorStore(...) {
    //     // Configure Redis-based vector store for persistence
    // }

}