package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
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
    private final ChatModel chatModel;

    public DocumentAiRepository(VectorStore vectorStore, PromptTemplate promptTemplate, ChatModel chatModel) {
        this.vectorStore = vectorStore;
        this.promptTemplate = promptTemplate;
        this.chatModel = chatModel;
    }

    public void addDocumentToVectorStore(Document document) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(document.getFile(), document.getDocumentName());
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(byteArrayResource);
        List<org.springframework.ai.document.Document> documents = tikaDocumentReader.get();

        TextSplitter textSplitter = new TokenTextSplitter();
        List<org.springframework.ai.document.Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }

    public Answer askQuestion(Question question, int topK, Double temperature) {
        List<org.springframework.ai.document.Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(question.question()).topK(topK).build());

        if (similarDocuments != null && !similarDocuments.isEmpty()) {
            List<String> contentList = similarDocuments.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .toList();
            Map<String, Object> promptParameters = new HashMap<>();
            promptParameters.put("input", question.question());
            promptParameters.put("documents", String.join("\n", contentList));
            Prompt prompt = promptTemplate.create(promptParameters);
            if (temperature != null) {
                prompt = new Prompt(prompt.getInstructions(),
                        OllamaChatOptions.builder().temperature(temperature).build());
            }
            ChatResponse response = chatModel.call(prompt);
            return new Answer(response.getResult().getOutput().getText());
        } else {
            return new Answer("Sorry, I don't have an answer for that question");
        }
    }
}
