package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest
public class DocumentReaderTests {

    @Value("classpath:/pdfs/WEF_Future_of_Jobs_Report_2025.pdf")
    private Resource resource;

    @Value("classpath:/spring-framework.html")
    private Resource springFrameworkHtml;

    @Test
    void retrieveContentFromUrl() {
        List<Document> documents = new JsoupDocumentReader(springFrameworkHtml).get();
        splitDocuments(documents);
    }

    @Test
    void retrieveContentFromPdfByPage() {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        List<Document> documents = reader.get();
        splitDocuments(documents);
    }

    @Test
    void retrieveContentFromPdfByParagraph() {
        try {
            ParagraphPdfDocumentReader reader = new ParagraphPdfDocumentReader(resource);
            List<Document> documents = reader.get();
            splitDocuments(documents);
        } catch (Exception e) {
            System.err.println("Error with ParagraphPdfDocumentReader: " + e.getMessage());
        }
    }

    private static void splitDocuments(List<Document> documents) {
        // Print information about the original documents
        System.out.println(documents.size() + " original documents before splitting");

        // Split the content into chunks
        var splitter = new TokenTextSplitter();
        List<Document> docs = splitter.split(documents);
        System.out.println(docs.size() + " documents after splitting");
    }
}
