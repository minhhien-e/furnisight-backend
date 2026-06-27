package com.furniro.MessageService.database.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furniro.MessageService.dto.MessageAttachment;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class MessageAttachmentListConverter implements AttributeConverter<List<MessageAttachment>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<MessageAttachment>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<MessageAttachment> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot serialize message attachments", e);
        }
    }

    @Override
    public List<MessageAttachment> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot deserialize message attachments", e);
        }
    }
}
