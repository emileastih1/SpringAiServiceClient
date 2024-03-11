package com.ea.ai.rag.dms.application.service;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface AiServiceClient {
    Answer askQuestion(Question question);

    void addDocumentToVectorStore(Document document);
}
