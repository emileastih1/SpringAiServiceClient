package com.ea.ai.rag.dms.application.service.impl;

import com.ea.ai.rag.dms.application.service.AiServiceClient;
import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.repository.DocumentAiClientRepository;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.stereotype.Service;

@Service
public class AiServiceClientImpl implements AiServiceClient {

    DocumentAiClientRepository documentAiClientRepository;

    public AiServiceClientImpl(DocumentAiClientRepository documentAiClientRepository) {
        this.documentAiClientRepository = documentAiClientRepository;
    }

    @Override
    public Answer askQuestion(Question question) {
        return documentAiClientRepository.askQuestion(question);
    }

    @Override
    public void addDocumentToVectorStore(Document document) {
        documentAiClientRepository.addDocumentToVectorStore(document);
    }
}
