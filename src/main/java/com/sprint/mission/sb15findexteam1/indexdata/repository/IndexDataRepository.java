package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataQueryRepository {

    // 동일한 지수 ID와 날짜의 데이터가 존재하는지 확인
    boolean existsByIndexInfoIdAndBaseDate(
        Long indexInfoId,
        LocalDate baseDate
    );

    // 특정 지수의 특정 날짜 데이터를 조회
    Optional<IndexData> findByIndexInfoIdAndBaseDate(
        Long indexInfoId,
        LocalDate baseDate
    );

    // 특정 지수의 기간별 데이터를 날짜 오름차순으로 조회
// TODO(D): 대시보드 조회 조건 및 정렬 방식 협의
    List<IndexData> findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
        Long indexInfoId,
        LocalDate startDate,
        LocalDate endDate
    );
}
