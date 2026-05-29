package com.ea.ai.rag.dms.presentation.exception;

import com.ea.ai.rag.dms.application.exception.FunctionalException;
import com.ea.ai.rag.dms.application.exception.MessageCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BackendExceptionHandlerTest {

    private final BackendExceptionHandler handler = new BackendExceptionHandler();

    @Test
    void handleFunctionalException_returns400WithCorrectErrorCode() {
        FunctionalException ex = new FunctionalException(MessageCode.DOCUMENT_NOT_FOUND, "Document not found");

        ApiErrorDto result = handler.handleFunctionalException(ex);

        assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.errorCode()).isEqualTo("DOCUMENT_NOT_FOUND");
        assertThat(result.title()).isEqualTo(ErrorMessageConstants.FUNCTIONAL_ERROR);
        assertThat(result.detail()).isEqualTo("Document not found");
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    void handleFunctionalException_includesAllMessageCodeValues() {
        for (MessageCode code : MessageCode.values()) {
            FunctionalException ex = new FunctionalException(code, "some detail");
            ApiErrorDto result = handler.handleFunctionalException(ex);
            assertThat(result.errorCode()).isEqualTo(code.name());
        }
    }

    @Test
    void handleForbiddenException_returns403WithForbiddenErrorCode() throws Exception {
        AccessDeniedException ex = new AccessDeniedException("Access denied to resource");

        ApiErrorDto result = handler.handleForbiddenException(ex);

        assertThat(result.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.errorCode()).isEqualTo(ErrorMessageConstants.ERROR_CODE_FORBIDDEN);
        assertThat(result.title()).isEqualTo(ErrorMessageConstants.FORBIDDEN_REQUEST_ACCESS);
        assertThat(result.detail()).isEqualTo("Access denied to resource");
    }

    @Test
    void handleMethodArgumentTypeMismatchException_returns400WithTypeMismatchCode() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("Failed to convert value");

        ApiErrorDto result = handler.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.errorCode()).isEqualTo(ErrorMessageConstants.ERROR_CODE_ARGUMENT_TYPE_MISMATCH);
        assertThat(result.title()).isEqualTo(ErrorMessageConstants.ERROR_ARGUMENT_TYPE_MISMATCH);
    }
}
