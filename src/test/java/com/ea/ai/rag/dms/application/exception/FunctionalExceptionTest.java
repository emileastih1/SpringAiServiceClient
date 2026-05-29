package com.ea.ai.rag.dms.application.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionalExceptionTest {

    @Test
    void functionalException_storesCodeAndMessageDetails() {
        FunctionalException exception = new FunctionalException(MessageCode.DOCUMENT_NOT_FOUND, "Document not found");

        assertThat(exception.getCode()).isEqualTo(MessageCode.DOCUMENT_NOT_FOUND);
        assertThat(exception.getMessageDetails()).isEqualTo("Document not found");
        assertThat(exception.getMessage()).isEqualTo("DOCUMENT_NOT_FOUND");
    }

    @Test
    void functionalException_isRuntimeException() {
        FunctionalException exception = new FunctionalException(MessageCode.INVALID_STATUS, "Invalid status");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @EnumSource(MessageCode.class)
    void functionalException_supportsAllMessageCodes(MessageCode code) {
        FunctionalException exception = new FunctionalException(code, "some detail");

        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(code.name());
    }
}
