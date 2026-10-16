package com.sprint.mission.sb15findexteam1.common.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.List;
import java.util.function.Function;

/**
 * 목록 API 4개(지수 정보, 지수 데이터, 연동 작업, 자동 연동 설정)가 함께 쓰는 커서 페이지 응답.
 * 필드 이름은 Swagger와 프론트엔드(frontend/src/model/pagination.ts)에 맞췄다.
 *
 * <pre>
 * {
 *   "content": [ ... ],
 *   "nextCursor": "코스피 200",   // 다음 요청의 cursor로 그대로 보내면 됨
 *   "nextIdAfter": 37,           // 다음 요청의 idAfter로 그대로 보내면 됨
 *   "size": 10,                  // 이번 페이지에 담긴 개수
 *   "totalElements": 152,        // 필터 조건에 맞는 전체 개수
 *   "hasNext": true
 * }
 * </pre>
 *
 * <p>사용법 (서비스에서)
 * <pre>
 * List&lt;XxxDto&gt; rows = repository.findPage(condition)   // size + 1개 조회한 결과
 *     .stream().map(mapper::toDto).toList();
 * long total = repository.count(condition);
 * return CursorPageResponseDto.of(rows, condition.size(), total,
 *     dto -&gt; 정렬필드값(dto), XxxDto::id);
 * </pre>
 */
public record CursorPageResponseDto<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

  /**
   * size + 1개를 조회한 결과로 페이지 응답을 만든다.
   *
   * <p>요청한 size보다 1개 더 조회해서, 남는 1개가 있으면 "다음 페이지가 있다"(hasNext)로 판단하고
   * 그 1개는 응답에서 뺀다. 개수 쿼리를 따로 날리지 않아도 다음 페이지 여부를 알 수 있다.
   *
   * @param fetched  size + 1개까지 조회한 결과
   * @param size     요청한 페이지 크기
   * @param total    커서를 뺀 필터 조건만으로 센 전체 개수
   * @param cursorOf 항목에서 정렬 기준 값을 꺼내는 함수 (예: dto -&gt; dto.indexName())
   * @param idOf     항목에서 id를 꺼내는 함수 (예: XxxDto::id)
   */
  public static <T> CursorPageResponseDto<T> of(List<T> fetched, int size, long total,
      Function<T, Object> cursorOf, Function<T, Long> idOf) {
    boolean hasNext = fetched.size() > size;
    List<T> content = hasNext ? fetched.subList(0, size) : fetched;

    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      T last = content.get(content.size() - 1);
      nextCursor = CursorPaging.toCursor(cursorOf.apply(last));
      nextIdAfter = idOf.apply(last);
    }
    return new CursorPageResponseDto<>(content, nextCursor, nextIdAfter, content.size(), total, hasNext);
  }
}
