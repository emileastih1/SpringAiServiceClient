package com.ea.ai.rag.dms.domain.dto;

import com.ea.ai.rag.dms.domain.vo.DocumentStatus;
import com.ea.ai.rag.dms.domain.vo.DocumentTypes;
import com.ea.ai.rag.dms.domain.vo.FileSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Document {

    private long id;

    @NotEmpty(message = "Document name is mandatory")
    @Schema(description = "Name of the document", example = "Test Document", requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentName;

    private DocumentTypes documentType;

    @NotEmpty(message = "File is mandatory")
    @Schema(description = "Base64 representation of the file", example = "base64string" ,requiredMode = Schema.RequiredMode.REQUIRED)
    private byte[] file;

    private String owner;

    private FileSize fileSize;

    private String location;

    private ZonedDateTime creationDate;

    private ZonedDateTime modificationDate;

    private DocumentStatus documentStatus;
}
