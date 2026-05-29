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
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisPooled;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfig {
    private final TextSplitter splitter = new TokenTextSplitter();

    @Value("classpath:/pdfs/WEF_Future_of_Jobs_Report_2025.pdf")
    private Resource jobsReport2025;

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

            // TODO: Implement document loading and processing
            // 1. Check if using Redis and if data already exists (for efficiency)
            // 2. Load documents from local course resources first:
            //    - spring-ai-course.md
            //    - future-of-jobs-summary.md
            //    - kendrick-drake-summary.md
            //    The full PDF and live web pages are optional extensions.
            // 3. Split documents using TextSplitter
            // 4. Add processed documents to vector store

            System.out.println("TODO: Document loading and vector store population not yet implemented");
            System.out.println("Available sources: " + springAiCourseNotes.getFilename() + ", "
                    + futureOfJobsSummary.getFilename() + ", " + kendrickDrakeSummary.getFilename()
                    + ", optional PDF: " + jobsReport2025.getFilename());
        };
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        // TODO: Create and configure SimpleVectorStore
        // This is the default in-memory vector store
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Profile("redis")
    VectorStore redisVectorStore(EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(new JedisPooled("localhost", 6379), embeddingModel)
                .indexName("spring-ai-index")
                .initializeSchema(true)
                .build();
    }

}
