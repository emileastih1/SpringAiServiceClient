package com.ea.ai.rag.dms.presentation.api;

import com.ea.ai.rag.dms.application.exception.FunctionalException;
import com.ea.ai.rag.dms.application.exception.MessageCode;
import com.ea.ai.rag.dms.application.service.AiServiceClient;
import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import com.ea.ai.rag.dms.presentation.exception.BackendExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiServiceClientRestControllerTest {

    @Mock
    private AiServiceClient aiServiceClient;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AiServiceClientRestController(aiServiceClient))
                .setControllerAdvice(new BackendExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void askQuestion_withValidQuestion_returns200WithAnswer() throws Exception {
        Question question = new Question("What is the document about?");
        when(aiServiceClient.askQuestion(any(Question.class), anyInt(), any())).thenReturn(new Answer("The document is about testing."));

        mockMvc.perform(post("/v1/document/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("The document is about testing."));
    }

    @Test
    void askQuestion_withBlankQuestion_returns400() throws Exception {
        Question question = new Question("");

        mockMvc.perform(post("/v1/document/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void askQuestion_whenServiceThrowsFunctionalException_returns400WithErrorCode() throws Exception {
        Question question = new Question("What is the document about?");
        when(aiServiceClient.askQuestion(any(Question.class), anyInt(), any()))
                .thenThrow(new FunctionalException(MessageCode.DOCUMENT_NOT_FOUND, "Document not found"));

        mockMvc.perform(post("/v1/document/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DOCUMENT_NOT_FOUND"));
    }

    @Test
    void askQuestion_withNoTopKParam_defaultsTopKToTwo() throws Exception {
        when(aiServiceClient.askQuestion(any(Question.class), anyInt(), any())).thenReturn(new Answer("answer"));

        mockMvc.perform(post("/v1/document/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is this?\"}"))
                .andExpect(status().isOk());

        verify(aiServiceClient).askQuestion(any(Question.class), eq(2), isNull());
    }

    @Test
    void askQuestion_withTopKParam_passesTopKToService() throws Exception {
        when(aiServiceClient.askQuestion(any(Question.class), anyInt(), any())).thenReturn(new Answer("answer"));

        mockMvc.perform(post("/v1/document/ask?topK=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is this?\"}"))
                .andExpect(status().isOk());

        verify(aiServiceClient).askQuestion(any(Question.class), eq(5), isNull());
    }

    @Test
    void addDocumentToVectorStore_withValidDocument_returns200() throws Exception {
        Document document = new Document();
        document.setDocumentName("Test Document");
        document.setFile("content".getBytes());
        doNothing().when(aiServiceClient).addDocumentToVectorStore(any(Document.class));

        mockMvc.perform(post("/v1/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(document)))
                .andExpect(status().isOk());
    }

    @Test
    void addDocumentToVectorStore_withMissingDocumentName_returns400() throws Exception {
        Document document = new Document();
        document.setFile("content".getBytes());

        mockMvc.perform(post("/v1/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(document)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addDocumentToVectorStore_withMissingFile_returns400() throws Exception {
        Document document = new Document();
        document.setDocumentName("Test Document");

        mockMvc.perform(post("/v1/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(document)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addDocumentToVectorStore_whenServiceThrowsFunctionalException_returns400() throws Exception {
        Document document = new Document();
        document.setDocumentName("Test Document");
        document.setFile("content".getBytes());
        doThrow(new FunctionalException(MessageCode.DOCUMENT_CANNOT_BE_ADDED, "Cannot add document"))
                .when(aiServiceClient).addDocumentToVectorStore(any(Document.class));

        mockMvc.perform(post("/v1/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(document)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DOCUMENT_CANNOT_BE_ADDED"));
    }
}
