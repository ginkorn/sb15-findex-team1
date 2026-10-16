package com.sprint.mission.sb15findexteam1.common.dto;

import com.sprint.mission.sb15findexteam1.common.pagination.CursorPaging;
import java.util.List;
import java.util.function.Function;

/**
 * 목록 API 4개(지수 정보, 지수 데이터, 연동 작업, 자동 연동 설정)가 함께 쓰는 커서 페이지 응답.
 * 필드 이름은 Swagger와 프론트엔드(frontend/src/model/pagination.ts)에 맞췄다. 바꾸면 화면이 깨진다.
 *
 * <pre>
 * {
 *   "content": [ ... ],          // 이번 페이지 데이터
 *   "nextCursor": "코스피 200",   // 다음 요청의 cursor 파라미터로 그대로 보내면 됨
 *   "nextIdAfter": 37,           // 다음 요청의 idAfter 파라미터로 그대로 보내면 됨
 *   "size": 10,                  // 이번 페이지에 실제로 담긴 개수
 *   "totalElements": 152,        // 필터 조건에 맞는 전체 개수
 *   "hasNext": true              // 다음 페이지가 있는지. false면 프론트가 더 요청하지 않음
 * }
 * </pre>
 *
 * <p>{@code <T>}(제네릭)는 "content에 담길 타입은 쓰는 쪽이 정한다"는 뜻이다.
 * 지수 정보 목록이면 {@code CursorPageResponseDto<IndexInfoDto>},
 * 지수 데이터 목록이면 {@code CursorPageResponseDto<IndexDataDto>}처럼 쓴다.
 *
 * <p><b>사용법 (서비스에서)</b>
 * <pre>
 * // 1. Repository에서 size + 1개를 조회해 DTO로 바꾼다
 * List&lt;IndexDataDto&gt; rows = indexDataRepository.findPageByCondition(condition)
 *     .stream().map(indexDataMapper::toDto).toList();
 *
 * // 2. 필터 조건만으로 전체 개수를 센다
 * long total = indexDataRepository.countByCondition(condition);
 *
 * // 3. of()에 넘기면 hasNext, nextCursor, nextIdAfter를 계산해 준다
 * return CursorPageResponseDto.of(rows, size, total,
 *     dto -&gt; dto.baseDate(),   // 정렬 기준 필드 값을 꺼내는 방법
 *     IndexDataDto::id);       // id를 꺼내는 방법
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
   * <p><b>왜 size + 1개를 조회하나?</b>
   * 10개를 요청받으면 11개를 조회한다. 11번째가 있으면 "다음 페이지가 있다"(hasNext = true)는 뜻이고,
   * 11번째는 응답에서 빼고 10개만 보낸다. 다음 페이지 여부를 알기 위해 쿼리를 한 번 더 날리지 않아도 된다.
   *
   * <p><b>다음 커서는 어떻게 만드나?</b>
   * 이번 페이지 마지막 항목의 "정렬 필드 값"이 nextCursor, "id"가 nextIdAfter가 된다.
   * 프론트엔드가 이 두 값을 다음 요청에 그대로 보내면, 그 항목 바로 다음부터 조회된다.
   *
   * <p>{@code Function<T, Object>}는 "T를 받아서 값을 꺼내는 방법"을 넘기는 자리다.
   * {@code dto -> dto.indexName()} 같은 람다나 {@code IndexInfoDto::id} 같은 메서드 참조를 넣으면 된다.
   *
   * @param fetched  Repository가 size + 1개까지 조회한 결과 (DTO로 변환한 것)
   * @param size     요청한 페이지 크기
   * @param total    커서를 뺀 필터 조건만으로 센 전체 개수
   * @param cursorOf 항목에서 정렬 기준 값을 꺼내는 함수 (예: dto -&gt; dto.indexName())
   * @param idOf     항목에서 id를 꺼내는 함수 (예: IndexInfoDto::id)
   */
  public static <T> CursorPageResponseDto<T> of(List<T> fetched, int size, long total,
      Function<T, Object> cursorOf, Function<T, Long> idOf) {
    // size보다 많이 조회됐으면 다음 페이지가 있다
    boolean hasNext = fetched.size() > size;
    // 다음 페이지 확인용으로 더 가져온 1개는 잘라낸다
    List<T> content = hasNext ? fetched.subList(0, size) : fetched;

    // 다음 페이지가 있을 때만 다음 커서를 만든다 (마지막 페이지면 둘 다 null)
    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      T last = content.get(content.size() - 1);
      nextCursor = CursorPaging.toCursor(cursorOf.apply(last)); // null이면 "__NULL__"
      nextIdAfter = idOf.apply(last);
    }
    return new CursorPageResponseDto<>(content, nextCursor, nextIdAfter, content.size(), total, hasNext);
  }
}
