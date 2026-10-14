package com.sprint.mission.sb15findexteam1.indexinfo.entity;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지수 정보 (DB 테이블: index_info).
 *
 * <p>"KOSPI시리즈 / 코스피"처럼 지수 하나의 기본 정보(명함)를 담는다.
 * 날짜별 시세는 IndexData, 자동 연동 on/off는 AutoSyncConfig가 따로 가지고 있고,
 * 두 테이블 모두 이 지수를 가리킨다.
 *
 * <p>규칙
 * <ul>
 *   <li>(지수 분류명 + 지수명) 조합은 중복될 수 없다. → 아래 uniqueConstraints</li>
 *   <li>수정할 수 있는 값은 채용 종목 수, 기준 시점, 기준 지수, 즐겨찾기 4개뿐이다. → update()</li>
 *   <li>값을 아무 데서나 바꾸지 못하도록 setter를 두지 않는다.</li>
 * </ul>
 */
@Entity // 이 클래스를 DB 테이블과 연결한다. 실행 시 JPA가 테이블을 만들어 준다.
@Getter // 모든 필드의 getXxx() 메서드를 Lombok이 자동으로 만들어 준다.
// JPA가 내부적으로 쓰는 빈 생성자. protected라서 다른 패키지에서 new IndexInfo()로 함부로 만들 수 없다.
// 객체는 아래 create() 메서드로만 만든다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "index_info",
    // 같은 분류명 + 지수명이 두 번 저장되면 DB가 거부한다.
    // (서비스에서 먼저 중복 검사를 하지만, 동시에 요청이 들어와도 막히도록 DB에도 건다)
    uniqueConstraints = @UniqueConstraint(
        name = "uk_index_info_classification_name",
        columnNames = {"index_classification", "index_name"}))
public class IndexInfo {

  /** 기본 키. 저장할 때 DB가 1, 2, 3... 순서로 자동 생성한다. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 지수 분류명. 예: "KOSPI시리즈" (Open API 필드: idxCsf) */
  @Column(nullable = false, length = 100)
  private String indexClassification;

  /** 지수명. 예: "코스피" (Open API 필드: idxNm) */
  @Column(nullable = false, length = 100)
  private String indexName;

  /** 채용 종목 수: 이 지수를 계산할 때 포함된 종목 개수 (Open API 필드: epyItmsCnt) */
  @Column(nullable = false)
  private Integer employedItemsCount;

  /** 기준 시점: 지수 계산의 기준이 되는 날짜 (Open API 필드: basPntm) */
  @Column(nullable = false)
  private LocalDate basePointInTime;

  /**
   * 기준 지수: 기준 시점의 지수 값. 예: 100 (Open API 필드: basIdx)
   * 금융 값이라 double 대신 BigDecimal을 쓴다. (double은 0.1 + 0.2 = 0.30000000000000004 같은 오차가 생긴다)
   * precision 19, scale 4 = 전체 19자리 중 소수점 아래 4자리 (README ERD 기준)
   */
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal baseIndex;

  /**
   * 출처: USER(사용자 등록) 또는 OPEN_API(연동 등록).
   * EnumType.STRING이라 DB에 "USER" 같은 글자로 저장된다. (숫자로 저장하면 enum 순서가 바뀔 때 데이터가 꼬인다)
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SourceType sourceType;

  /** 즐겨찾기 여부. 대시보드의 "관심 지수 성과"는 이 값이 true인 지수만 보여 준다. */
  @Column(nullable = false)
  private boolean favorite;

  /**
   * 새 지수 정보를 만든다.
   *
   * <p>주의: 지수를 저장할 때는 자동 연동 설정(AutoSyncConfig)도 반드시 함께 만들어야 한다.
   * 그 작업은 서비스(IndexInfoService)에서 한 곳에 모아 처리한다.
   *
   * @param sourceType 사용자 등록이면 USER, Open API 연동이면 OPEN_API
   */
  public static IndexInfo create(String indexClassification, String indexName,
      Integer employedItemsCount, LocalDate basePointInTime, BigDecimal baseIndex,
      SourceType sourceType, boolean favorite) {
    IndexInfo indexInfo = new IndexInfo();
    indexInfo.indexClassification = indexClassification;
    indexInfo.indexName = indexName;
    indexInfo.employedItemsCount = employedItemsCount;
    indexInfo.basePointInTime = basePointInTime;
    indexInfo.baseIndex = baseIndex;
    indexInfo.sourceType = sourceType;
    indexInfo.favorite = favorite;
    return indexInfo;
  }

  /**
   * 지수 정보를 수정한다. (PATCH 방식: 보낸 값만 바꾼다)
   *
   * <p>null로 들어온 값은 "바꾸지 않음"으로 보고 기존 값을 그대로 둔다.
   * 분류명과 지수명은 요구사항상 수정할 수 없어서 파라미터에 없다.
   *
   * <p>JPA는 트랜잭션 안에서 엔티티 값이 바뀌면 끝날 때 자동으로 UPDATE 쿼리를 보낸다.
   * (save()를 다시 부르지 않아도 된다. 이를 "변경 감지"라고 한다)
   *
   * @param favorite Open API 연동으로 수정할 때는 사용자가 정한 값이 지워지지 않도록 null을 넘긴다
   */
  public void update(Integer employedItemsCount, LocalDate basePointInTime,
      BigDecimal baseIndex, Boolean favorite) {
    if (employedItemsCount != null) {
      this.employedItemsCount = employedItemsCount;
    }
    if (basePointInTime != null) {
      this.basePointInTime = basePointInTime;
    }
    if (baseIndex != null) {
      this.baseIndex = baseIndex;
    }
    if (favorite != null) {
      this.favorite = favorite;
    }
  }
}
