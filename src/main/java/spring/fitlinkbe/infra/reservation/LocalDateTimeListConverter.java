package spring.fitlinkbe.infra.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import spring.fitlinkbe.domain.common.exception.CustomException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.FAILED_TO_CONVERT_JSON;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.FAILED_TO_CONVERT_LIST;

@Converter(autoApply = true)
public class LocalDateTimeListConverter implements AttributeConverter<List<LocalDateTime>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Java 8 LocalDateTime 지원
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 포맷 유지

    @Override
    public String convertToDatabaseColumn(List<LocalDateTime> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]"; // 빈 리스트는 빈 JSON 배열로 저장
        }
        try {
            return objectMapper.writeValueAsString(attribute); // JSON 문자열 변환
        } catch (Exception e) {
            throw new CustomException(FAILED_TO_CONVERT_JSON, e.getMessage());
        }
    }

    @Override
    public List<LocalDateTime> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList(); // null 또는 빈 문자열이면 빈 리스트 반환
        }
        try {
            // DB에서 가져온 값이 이스케이프된 문자열이면 다시 JSON으로 변환
            if (dbData.startsWith("\"") && dbData.endsWith("\"")) {
                dbData = objectMapper.readValue(dbData, String.class); // 이스케이프 제거
            }
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new CustomException(FAILED_TO_CONVERT_LIST, e.getMessage());
        }
    }
}
