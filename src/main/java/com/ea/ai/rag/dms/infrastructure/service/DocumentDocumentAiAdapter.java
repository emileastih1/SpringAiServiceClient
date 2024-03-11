package com.ea.ai.rag.dms.infrastructure.service;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.repository.DocumentAiClientRepository;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import com.ea.ai.rag.dms.infrastructure.repository.DocumentAiRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentDocumentAiAdapter implements DocumentAiClientRepository {
    DocumentAiRepository documentAiRepository;

    public DocumentDocumentAiAdapter(DocumentAiRepository documentAiRepository) {
        this.documentAiRepository = documentAiRepository;
    }
    @Override
    public Answer askQuestion(Question question) {
        return documentAiRepository.askQuestion(question);
    }

    @Override
    public void addDocumentToVectorStore(Document document) {
        documentAiRepository.addDocumentToVectorStore(document);
    }
}
