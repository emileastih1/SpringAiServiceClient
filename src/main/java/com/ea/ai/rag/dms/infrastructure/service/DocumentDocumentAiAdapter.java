package com.ea.ai.rag.dms.infrastructure.service;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.repository.DocumentAiClientRepository;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import com.ea.ai.rag.dms.infrastructure.repository.DocumentAiRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class DocumentDocumentAiAdapter implements DocumentAiClientRepository {
    DocumentAiRepository documentAiRepository;

    public DocumentDocumentAiAdapter(DocumentAiRepository documentAiRepository) {
        this.documentAiRepository = documentAiRepository;
    }

    @Override
    public Answer askQuestion(Question question, int topK, Double temperature) {
        return documentAiRepository.askQuestion(question, topK, temperature);
    }

    @Override
    public void addDocumentToVectorStore(Document document) {
        documentAiRepository.addDocumentToVectorStore(document);
    }

    @Override
    public Flux<String> streamAnswer(Question question, int topK, Double temperature) {
        return documentAiRepository.streamAnswer(question, topK, temperature);
    }

    @Override
    public Flux<String> streamAnswer(Question question, int topK, Double temperature, List<Long> documentIds) {
        return documentAiRepository.streamAnswer(question, topK, temperature, documentIds);
    }

    @Override
    public void deleteChunksByDocumentId(long documentId) {
        documentAiRepository.deleteChunksByDocumentId(documentId);
    }

    @Override
    public String classifySentiment(String content) {
        return documentAiRepository.classifySentiment(content);
    }
}
