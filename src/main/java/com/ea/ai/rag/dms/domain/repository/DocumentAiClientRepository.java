package com.ea.ai.rag.dms.domain.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentAiClientRepository {
    Answer askQuestion(Question question);

    void addDocumentToVectorStore(Document document);
}
