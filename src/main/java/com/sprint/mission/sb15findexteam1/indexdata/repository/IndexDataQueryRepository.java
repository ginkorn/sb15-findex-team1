
package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.sprint.mission.sb15findexteam1.indexdata.dto.IndexDataSearchCondition;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.util.List;

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
}
