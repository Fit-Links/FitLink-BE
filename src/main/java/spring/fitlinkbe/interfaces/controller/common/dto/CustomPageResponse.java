package spring.fitlinkbe.interfaces.controller.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class CustomPageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    @Builder
    public CustomPageResponse(List<T> content, int totalPages, long totalElements, boolean hasNext) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }

    public CustomPageResponse() {
    }

    public static <E, R> CustomPageResponse<R> of(Page<E> page, Function<E, R> mapper) {
        return CustomPageResponse.<R>builder()
                .content(page.getContent().stream()
                        .map(mapper)
                        .collect(Collectors.toList()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }

    public static <E> CustomPageResponse<E> from(Page<E> page) {
        return CustomPageResponse.<E>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }
}
