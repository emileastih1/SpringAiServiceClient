package com.ea.ai.rag.dms.presentation.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiErrorDtoTest {

    @Test
    void apiErrorDto_createsWithValidFields() {
        ApiErrorDto dto = new ApiErrorDto("Error Title", "ERROR_CODE", 400, "Error detail");

        assertThat(dto.title()).isEqualTo("Error Title");
        assertThat(dto.errorCode()).isEqualTo("ERROR_CODE");
        assertThat(dto.status()).isEqualTo(400);
        assertThat(dto.detail()).isEqualTo("Error detail");
        assertThat(dto.timestamp()).isNotNull();
    }

    @Test
    void apiErrorDto_throwsWhenTitleIsBlank() {
        assertThatThrownBy(() -> new ApiErrorDto("", "ERROR_CODE", 400, "Error detail"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apiErrorDto_throwsWhenErrorCodeIsBlank() {
        assertThatThrownBy(() -> new ApiErrorDto("Error Title", "", 400, "Error detail"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apiErrorDto_throwsWhenDetailIsBlank() {
        assertThatThrownBy(() -> new ApiErrorDto("Error Title", "ERROR_CODE", 400, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apiErrorDto_throwsWhenTitleIsWhitespace() {
        assertThatThrownBy(() -> new ApiErrorDto("   ", "ERROR_CODE", 400, "Error detail"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
