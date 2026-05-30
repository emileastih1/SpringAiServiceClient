package com.ea.ai.rag.dms.application.service;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Transactional
public interface AiServiceClient {
    Answer askQuestion(Question question, int topK, Double temperature);

    void addDocumentToVectorStore(Document document);

    Flux<String> streamAnswer(Question question, int topK, Double temperature);

    Flux<String> streamAnswer(Question question, int topK, Double temperature, List<Long> documentIds);

    void deleteChunksByDocumentId(long documentId);

    String classifySentiment(String content);

    void embedContent(long documentId, String documentName, String content);
}
