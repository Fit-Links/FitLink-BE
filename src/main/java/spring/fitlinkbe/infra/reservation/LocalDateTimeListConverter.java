package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class LocalDateTimeListConverter implements AttributeConverter<List<LocalDateTime>, String> {

    @Override
    public String convertToDatabaseColumn(List<LocalDateTime> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]"; // 빈 리스트는 빈 JSON 배열로 저장
        }

        return attribute.stream()
                .map(LocalDateTime::toString)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<LocalDateTime> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList(); // null 또는 빈 문자열이면 빈 리스트 반환
        }
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(LocalDateTime::parse)
                .collect(Collectors.toList());
    }
}
