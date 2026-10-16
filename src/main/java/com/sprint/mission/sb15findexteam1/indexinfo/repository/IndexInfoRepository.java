package com.sprint.mission.sb15findexteam1.indexinfo.repository;

import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 지수 정보 저장소 (DB 창고 담당).
 *
 * <p>두 가지를 함께 상속해서 한 Repository로 쓴다.
 * <ul>
 *   <li>{@code JpaRepository<IndexInfo, Long>}: save, findById, findAll, delete 같은 기본 기능을
 *       스프링이 자동으로 만들어 준다. (IndexInfo = 다룰 엔티티, Long = id 타입)</li>
 *   <li>{@code IndexInfoQueryRepository}: 조건이 많은 목록 검색. 구현은 IndexInfoQueryRepositoryImpl(QueryDSL)이
 *       맡는다. 이름 끝이 "Impl"이면 스프링이 자동으로 연결해 준다.</li>
 * </ul>
 *
 * <p>아래 메서드는 <b>이름만으로</b> 쿼리가 만들어진다. (Spring Data JPA의 "쿼리 메서드")
 * 예: existsByIndexClassificationAndIndexName
 *   → "indexClassification과 indexName이 둘 다 같은 행이 있는가?"
 *
 * <p>B·C가 쓸 수 있는 메서드
 * <ul>
 *   <li>{@code existsById(id)}: 지수 데이터 등록 전에 지수가 실제로 있는지 확인 (B)</li>
 *   <li>{@code findByIndexClassificationAndIndexName}: Open API 응답의 지수가 이미 있는지 확인 (C)</li>
 * </ul>
 */
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoQueryRepository {

  /** 같은 (분류명 + 지수명)이 이미 있는지 (등록 전 중복 검사) */
  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

  /** (분류명 + 지수명)으로 지수 찾기. 없을 수 있어서 Optional로 감싸서 돌려준다. */
  Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);
}
