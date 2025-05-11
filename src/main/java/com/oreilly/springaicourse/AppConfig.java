package com.oreilly.springaicourse;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class AppConfig {
    private static final String FEUD_URL = "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud";
    private static final String SPRING_URL = "https://en.wikipedia.org/wiki/Spring_Framework";

    private final TextSplitter splitter = new TokenTextSplitter();

    @Bean
    @Profile("rag")
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> List.of(FEUD_URL, SPRING_URL).forEach(url -> {
            // Fetch HTML content using Jsoup
            List<Document> documents = new JsoupDocumentReader(url).get();
            System.out.println("Fetched " + documents.size() + " documents from " + url);

            // Split the document into chunks
            List<Document> chunks = splitter.apply(documents);

            // Add the chunks to the vector store
            vectorStore.add(chunks);
        });
    }

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
