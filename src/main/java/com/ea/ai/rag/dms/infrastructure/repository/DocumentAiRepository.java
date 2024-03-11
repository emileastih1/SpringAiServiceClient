package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentAiRepository {
    private final VectorStore vectorStore;
    private final PromptTemplate promptTemplate;
    private final ChatClient aiChatClient;

    public DocumentAiRepository(VectorStore vectorStore, PromptTemplate promptTemplate, ChatClient aiChatClient) {
        this.vectorStore = vectorStore;
        this.promptTemplate = promptTemplate;
        this.aiChatClient = aiChatClient;
    }

    public void addDocumentToVectorStore(Document document) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(document.getFile(), document.getDocumentName());
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(byteArrayResource);
        List<org.springframework.ai.document.Document> documents = tikaDocumentReader.get();

        TextSplitter textSplitter = new TokenTextSplitter();
        List<org.springframework.ai.document.Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }

    public Answer askQuestion(Question question) {
        List<org.springframework.ai.document.Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question.question()).withTopK(2));
        if (similarDocuments != null && !similarDocuments.isEmpty()) {
            List<String> contentList = similarDocuments.stream().map(org.springframework.ai.document.Document::getContent).toList();
            Map<String, Object> promptParameters = new HashMap<>();
            promptParameters.put("input", question.question());
            promptParameters.put("documents", String.join("\n", contentList));
            Prompt prompt = promptTemplate.create(promptParameters);
            ChatResponse response = aiChatClient.call(prompt);
            return new Answer(response.getResult().getOutput().getContent());
        } else {
            return new Answer("Sorry, I don't have an answer for that question");
        }
    }
}
