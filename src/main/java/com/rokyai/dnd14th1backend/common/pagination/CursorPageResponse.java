package com.rokyai.dnd14th1backend.common.pagination;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cursor 기반 페이지네이션 응답.
 *
 * @param <T> 항목 타입
 * @param content 현재 페이지의 항목 목록
 * @param nextCursor 다음 페이지 요청에 사용할 cursor (마지막 페이지이면 null)
 * @param hasNext 다음 페이지 존재 여부
 * @param size 요청한 페이지 크기
 */
@Schema(description = "Cursor 기반 페이지네이션 응답")
public record CursorPageResponse<T>(
        @Schema(description = "항목 목록") List<T> content,
        @Schema(description = "다음 페이지 cursor (마지막 페이지이면 null)", nullable = true) UUID nextCursor,
        @Schema(description = "다음 페이지 존재 여부") boolean hasNext,
        @Schema(description = "페이지 크기") int size) {

    /**
     * size+1개 조회 결과로 CursorPageResponse를 생성한다.
     *
     * @param fetchedItems size+1개까지 조회된 항목 리스트
     * @param requestedSize 요청한 페이지 크기
     * @param idExtractor 항목에서 ID를 추출하는 함수
     * @return CursorPageResponse
     */
    public static <T> CursorPageResponse<T> of(
            List<T> fetchedItems,
            int requestedSize,
            java.util.function.Function<T, UUID> idExtractor) {
        boolean hasNext = fetchedItems.size() > requestedSize;
        List<T> content = hasNext ? fetchedItems.subList(0, requestedSize) : fetchedItems;
        UUID nextCursor = hasNext ? idExtractor.apply(content.get(content.size() - 1)) : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext, requestedSize);
    }
}
