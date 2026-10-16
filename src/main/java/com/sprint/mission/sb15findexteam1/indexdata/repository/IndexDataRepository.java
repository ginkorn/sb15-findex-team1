package com.sprint.mission.sb15findexteam1.indexdata.repository;

import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 지수 정보 삭제 시 그 지수의 데이터를 한 번에 삭제 (A의 IndexInfoService에서 사용)
    // IndexData는 indexInfoId만 가지고 있어 DB가 자동으로 지워 주지 않기 때문에 직접 삭제한다
    @Modifying(clearAutomatically = true)
    @Query("delete from IndexData d where d.indexInfoId = :indexInfoId")
    int deleteAllByIndexInfoIdInBulk(@Param("indexInfoId") Long indexInfoId);

    // 특정 지수의 기간별 데이터를 날짜 오름차순으로 조회
    // TODO(D): 대시보드 조회 조건 및 정렬 방식 협의
    List<IndexData> findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
        Long indexInfoId,
        LocalDate startDate,
        LocalDate endDate
    );
}
