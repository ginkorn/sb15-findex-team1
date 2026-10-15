package com.sprint.mission.sb15findexteam1.dashboard.repository;

import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.Optional;

public interface DashboardQueryRepository {

  // 지수의 데이터 중 기준일 이전(기준일 포함)에서 가장 최근 1건을 조회
  // 기준일에 데이터가 없으면(주말, 휴일) 그 이전 중 가장 가까운 날을 돌려줌
  Optional<IndexData> findLatestOnOrBefore(Long indexInfoId, LocalDate baseDate);
}
