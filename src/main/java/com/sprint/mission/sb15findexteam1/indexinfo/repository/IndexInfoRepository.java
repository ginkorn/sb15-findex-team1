package com.sprint.mission.sb15findexteam1.indexinfo.repository;

import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 지수 정보 저장소. 기본 CRUD는 JpaRepository가, 목록 검색(필터·정렬·커서)은 IndexInfoQueryRepository가 담당한다.
 *
 * <p>B·C가 쓸 수 있는 메서드
 * <ul>
 *   <li>{@code existsById(id)}: 지수 데이터 등록 전 지수가 실제로 있는지 확인</li>
 *   <li>{@code findByIndexClassificationAndIndexName}: Open API 응답의 지수가 이미 있는지 확인</li>
 * </ul>
 */
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoQueryRepository {

  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

  Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);
}
