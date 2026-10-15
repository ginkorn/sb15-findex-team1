package com.sprint.mission.sb15findexteam1.indexdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 지수 시세 데이터를 DB의 index_data 테이블에 저장하는 Entity
@Getter
@Entity
@Table(
    name = "index_data",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_index_data_info_date",
        columnNames = {"index_info_id", "base_date"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData {

  // 지수 데이터의 고유 ID (DB에서 자동 생성)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(A): IndexInfo Entity 완성 후 연관관계로 변경
  // 어떤 지수에 속한 데이터인지 구분하는 ID
  @Column(name = "index_info_id", nullable = false)
  private Long indexInfoId;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  // TODO(A): common.enums.SourceType 완성 후 @Enumerated(EnumType.STRING)으로 변경
  @Column(name = "source_type", length = 20, nullable = false)
  private String sourceType;

  @Column(name = "market_price", precision = 19, scale = 4)
  private BigDecimal marketPrice;

  @Column(name = "closing_price", precision = 19, scale = 4)
  private BigDecimal closingPrice;

  @Column(name = "high_price", precision = 19, scale = 4)
  private BigDecimal highPrice;

  @Column(name = "low_price", precision = 19, scale = 4)
  private BigDecimal lowPrice;

  @Column(name = "versus", precision = 19, scale = 4)
  private BigDecimal versus;

  @Column(name = "fluctuation_rate", precision = 10, scale = 4)
  private BigDecimal fluctuationRate;

  @Column(name = "trading_quantity")
  private Long tradingQuantity;

  @Column(name = "trading_price")
  private Long tradingPrice;

  @Column(name = "market_total_amount")
  private Long marketTotalAmount;

  @Builder
  public IndexData(
      Long indexInfoId,
      LocalDate baseDate,
      String sourceType,
      BigDecimal marketPrice,
      BigDecimal closingPrice,
      BigDecimal highPrice,
      BigDecimal lowPrice,
      BigDecimal versus,
      BigDecimal fluctuationRate,
      Long tradingQuantity,
      Long tradingPrice,
      Long marketTotalAmount
  ) {
      this.indexInfoId = indexInfoId;
      this.baseDate = baseDate;
      this.sourceType = sourceType;
      this.marketPrice = marketPrice;
      this.closingPrice = closingPrice;
      this.highPrice = highPrice;
      this.lowPrice = lowPrice;
      this.versus = versus;
      this.fluctuationRate = fluctuationRate;
      this.tradingQuantity = tradingQuantity;
      this.tradingPrice = tradingPrice;
      this.marketTotalAmount = marketTotalAmount;
  }

  // 기존 지수 데이터의 시세 및 거래 정보를 수정
  // PATCH 요청이므로 null로 전달된 필드는 기존 값을 유지
  public void update(
      BigDecimal marketPrice,
      BigDecimal closingPrice,
      BigDecimal highPrice,
      BigDecimal lowPrice,
      BigDecimal versus,
      BigDecimal fluctuationRate,
      Long tradingQuantity,
      Long tradingPrice,
      Long marketTotalAmount
  ) {

    // 시가가 전달되었다면 수정
    if (marketPrice != null) {
      this.marketPrice = marketPrice;
    }

    // 종가가 전달되었다면 수정
    if (closingPrice != null) {
      this.closingPrice = closingPrice;
    }

    // 고가가 전달되었다면 수정
    if (highPrice != null) {
      this.highPrice = highPrice;
    }

    // 저가가 전달되었다면 수정
    if (lowPrice != null) {
      this.lowPrice = lowPrice;
    }

    // 전일 대비 값이 전달되었다면 수정
    if (versus != null) {
      this.versus = versus;
    }

    // 등락률이 전달되었다면 수정
    if (fluctuationRate != null) {
      this.fluctuationRate = fluctuationRate;
    }

    // 거래량이 전달되었다면 수정
    if (tradingQuantity != null) {
      this.tradingQuantity = tradingQuantity;
    }

    // 거래대금이 전달되었다면 수정
    if (tradingPrice != null) {
      this.tradingPrice = tradingPrice;
    }

    // 시가총액이 전달되었다면 수정
    if (marketTotalAmount != null) {
      this.marketTotalAmount = marketTotalAmount;
    }
  }

}