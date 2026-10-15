package com.sprint.mission.sb15findexteam1.dashboard.repository;

import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData,Long> {

  // 기간 내 데이터를 날짜 오름차순으로 조회
  List<IndexData> findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate from, LocalDate to);

  // 지수의 가장 최근 데이터 1건
  Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long indexInfoId);

}
