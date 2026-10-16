package com.sprint.mission.sb15findexteam1.dashboard.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.sb15findexteam1.indexdata.entity.IndexData;
import com.sprint.mission.sb15findexteam1.indexdata.entity.QIndexData;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.QIndexInfo;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardQueryRepositoryImpl implements DashboardQueryRepository {

  // QueryDSL이 자동 생성한 지수 데이터 조회용 클래스 (data.baseDate 처럼 컬럼을 코드로 가리킴)
  private static final QIndexData data = QIndexData.indexData;
  // QueryDSL이 자동 생성한 지수 정보 조회용 클래스 (info.favorite 처럼 컬럼을 코드로 가리킴)
  private static final QIndexInfo info = QIndexInfo.indexInfo;

  // JPA를 이용해 쿼리를 만들어 주는 객체
  private final JPAQueryFactory queryFactory;

  // EntityManager를 주입받아 QueryDSL 사용 준비
  public DashboardQueryRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public Optional<IndexData> findLatestOnOrBefore(Long indexInfoId, LocalDate baseDate) {
    IndexData result = queryFactory
        .selectFrom(data)                          // index_data 테이블에서 조회
        .where(
            data.indexInfoId.eq(indexInfoId),      // 이 지수의 데이터만
            data.baseDate.loe(baseDate))           // 기준일 이하(기준일 포함)
        .orderBy(data.baseDate.desc())             // 날짜 최신순으로 정렬
        .fetchFirst();                             // 맨 위 1건만 (없으면 null)

    return Optional.ofNullable(result);            // null일 수 있으니 Optional로 감싸서 반환
  }

  @Override
  public List<IndexInfo> findFavoriteIndexInfos() {
    return queryFactory
        .selectFrom(info)                  // index_info 테이블에서 조회
        .where(info.favorite.isTrue())     // 즐겨찾기가 true인 것만
        .orderBy(info.id.asc())            // id 오름차순 (등록 순서)
        .fetch();                          // 결과를 목록으로 받음 (없으면 빈 목록)
  }
}