
package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.util.List;

// QueryDSL로 구현할 지수 데이터 조회 기능을 정의하는 인터페이스
public interface IndexDataQueryRepository {

  // 검색 조건에 맞는 지수 데이터를 커서 페이지네이션으로 조회
  // size + 1개를 조회하여 다음 페이지 존재 여부를 판단
  List<IndexData> findPageByCondition(
      IndexDataSearchCondition condition
  );

  // CSV 다운로드를 위해 페이지네이션 없이 전체 데이터를 조회
  List<IndexData> findForCsv(
      IndexDataSearchCondition condition
  );

  // 검색 조건에 해당하는 전체 데이터 개수를 조회
  long countByCondition(
      IndexDataSearchCondition condition
  );

  // TODO(A): 공통 커서 페이지 응답 DTO 확정 후 Service에서 연결
  // TODO(A): nextCursor, nextIdAfter, hasNext 응답 조립 방식 협의
}
