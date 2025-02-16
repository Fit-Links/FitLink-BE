package spring.fitlinkbe.support.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class LocalTimeListConverter implements AttributeConverter<List<LocalTime>, String> {

    @Override
    public String convertToDatabaseColumn(List<LocalTime> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        // 각 LocalTime 을 문자열로 변환 후, 쉼표로 join
        return attribute.stream()
                .map(LocalTime::toString)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<LocalTime> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 쉼표로 구분된 문자열을 분리하여 각각 LocalTime 으로 파싱
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(LocalTime::parse)
                .collect(Collectors.toList());
    }
}