package spring.fitlinkbe.interfaces.controller.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class CustomPageResponse<T> {
    private final List<T> content;
    private final int totalPages;
    private final int totalElements;
}
