package com.ea.ai.rag.dms.application.service.impl;

import com.ea.ai.rag.dms.application.service.AiServiceClient;
import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.repository.DocumentAiClientRepository;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class AiServiceClientImpl implements AiServiceClient {

    DocumentAiClientRepository documentAiClientRepository;

    public AiServiceClientImpl(DocumentAiClientRepository documentAiClientRepository) {
        this.documentAiClientRepository = documentAiClientRepository;
    }

    @Override
    public Answer askQuestion(Question question, int topK, Double temperature) {
        return documentAiClientRepository.askQuestion(question, topK, temperature);
    }

    @Override
    public void addDocumentToVectorStore(Document document) {
        documentAiClientRepository.addDocumentToVectorStore(document);
    }

    @Override
    public Flux<String> streamAnswer(Question question, int topK, Double temperature) {
        return documentAiClientRepository.streamAnswer(question, topK, temperature);
    }

    @Override
    public Flux<String> streamAnswer(Question question, int topK, Double temperature, List<Long> documentIds) {
        return documentAiClientRepository.streamAnswer(question, topK, temperature, documentIds);
    }

    @Override
    public void deleteChunksByDocumentId(long documentId) {
        documentAiClientRepository.deleteChunksByDocumentId(documentId);
    }

    @Override
    public String classifySentiment(String content) {
        return documentAiClientRepository.classifySentiment(content);
    }

    @Override
    public void embedContent(long documentId, String documentName, String content) {
        documentAiClientRepository.embedContent(documentId, documentName, content);
    }
}
