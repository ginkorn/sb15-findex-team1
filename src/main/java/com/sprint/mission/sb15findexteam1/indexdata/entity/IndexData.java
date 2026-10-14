package com.sprint.mission.sb15findexteam1.indexdata.entity;

import com.sprint.mission.sb15findexteam1.common.enums.SourceType;
import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 지수 데이터 (DB 테이블: index_data).
 *
 * <p>지수의 "날짜별 시세"다. 예: 2026-10-13 코스피의 시가·종가·고가·저가·거래량 등.
 * 대시보드의 차트, 이동평균선, 성과 랭킹이 모두 이 테이블의 종가(closingPrice)로 계산된다.
 *
 * <p>규칙
 * <ul>
 *   <li>(지수 + 기준 일자) 조합은 중복될 수 없다. → 같은 날 시세는 하나뿐</li>
 *   <li>지수 정보가 삭제되면 그 지수의 시세도 모두 삭제된다.</li>
 * </ul>
 *
 * <p>담당 안내: 필드와 제약(테이블 모양)만 A가 정의했다.
 * 생성·수정 메서드(create, update, Open API용 upsert 등)는 담당자(B)가 기능에 맞게 추가한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 빈 생성자. 생성 메서드는 B가 추가 예정.
@Table(
    name = "index_data",
    // 같은 지수의 같은 날짜 시세가 두 번 저장되면 DB가 거부한다.
    // (Open API 연동을 여러 번 실행해도 중복 행이 생기지 않게 하는 마지막 안전장치)
    uniqueConstraints = @UniqueConstraint(
        name = "uk_index_data_index_info_base_date",
        columnNames = {"index_info_id", "base_date"}))
public class IndexData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 이 시세가 속한 지수.
   *
   * <ul>
   *   <li>{@code @ManyToOne}: 지수 하나에 시세가 여러 개 (날짜마다 하나씩)</li>
   *   <li>{@code FetchType.LAZY}: 시세를 조회할 때 지수 정보는 필요할 때만 가져온다.</li>
   *   <li>{@code @OnDelete(CASCADE)}: 지수가 삭제되면 DB가 이 시세들도 함께 삭제한다.</li>
   * </ul>
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private IndexInfo indexInfo;

  /** 기준 일자: 이 시세의 날짜 (Open API 필드: basDt, "20261013" 형식 문자열) */
  @Column(nullable = false)
  private LocalDate baseDate;

  /** 출처: USER(사용자 등록) 또는 OPEN_API(연동 등록) */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SourceType sourceType;

  // ---- 가격 정보: 금융 값이라 모두 BigDecimal, 자릿수는 README ERD 기준 ----

  /** 시가: 장이 열린 뒤 처음 형성된 가격 (Open API 필드: mkp) */
  @Column(precision = 19, scale = 4)
  private BigDecimal marketPrice;

  /** 종가: 장이 끝날 때까지 형성된 마지막 가격. 차트·이동평균·성과 계산의 기준 (Open API 필드: clpr) */
  @Column(precision = 19, scale = 4)
  private BigDecimal closingPrice;

  /** 고가: 하루 중 가장 높은 값 (Open API 필드: hipr) */
  @Column(precision = 19, scale = 4)
  private BigDecimal highPrice;

  /** 저가: 하루 중 가장 낮은 값 (Open API 필드: lopr) */
  @Column(precision = 19, scale = 4)
  private BigDecimal lowPrice;

  /** 대비: 전일 대비 오르내린 값. 음수면 하락 (Open API 필드: vs) */
  @Column(precision = 19, scale = 4)
  private BigDecimal versus;

  /** 등락률: 전일 대비 오르내린 비율(%). 값의 범위가 작아서 precision 10 (Open API 필드: fltRt) */
  @Column(precision = 10, scale = 4)
  private BigDecimal fluctuationRate;

  // ---- 거래 정보: 소수점이 없는 큰 정수라 Long ----

  /** 거래량: 지수에 포함된 종목의 거래량 합계 (Open API 필드: trqu) */
  private Long tradingQuantity;

  /** 거래대금: 지수에 포함된 종목의 거래대금 합계 (Open API 필드: trPrc) */
  private Long tradingPrice;

  /** 상장 시가총액: 지수에 포함된 종목의 시가총액 합계 (Open API 필드: lstgMrktTotAmt) */
  private Long marketTotalAmount;
}
