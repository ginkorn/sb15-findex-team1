package com.sprint.mission.sb15findexteam1.syncjob.entity;

import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import com.sprint.mission.sb15findexteam1.syncjob.enums.JobType;
import com.sprint.mission.sb15findexteam1.syncjob.enums.ResultType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 연동 작업 이력 (DB 테이블: sync_job).
 *
 * <p>Open API 연동을 실행할 때마다 남기는 "영수증"이다.
 * 누가(worker), 언제(jobTime), 어떤 지수(indexInfo)의 어느 날짜(targetDate)를 연동했고,
 * 성공했는지 실패했는지(result)를 기록한다. 연동 관리 화면의 이력 목록이 이 테이블을 보여 준다.
 *
 * <p>규칙
 * <ul>
 *   <li>지수 데이터 연동은 "지수 × 날짜"마다 1건씩 기록한다.</li>
 *   <li>실패한 작업도 FAILED로 반드시 기록한다.</li>
 * </ul>
 *
 * <p>담당 안내: 필드(테이블 모양)만 A가 정의했다.
 * 이력을 만드는 생성 메서드는 담당자(C)가 추가하고, 목록 조회는 D가 사용한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 빈 생성자.
@Table(name = "sync_job")
public class SyncJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 작업 종류: INDEX_INFO(지수 정보 연동) 또는 INDEX_DATA(지수 데이터 연동) */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private JobType jobType;

  /**
   * 연동한 지수.
   *
   * <ul>
   *   <li>{@code @ManyToOne}: 지수 하나에 이력이 여러 개</li>
   *   <li>nullable: 지수가 삭제된 뒤에도 이력은 남아야 하므로 비어 있을 수 있다.</li>
   *   <li>{@code @OnDelete(SET_NULL)}: 지수가 삭제되면 이력은 지우지 않고 이 칸만 null로 비운다.
   *       (영수증은 물건을 버려도 보관하는 것처럼, "언제 무엇을 연동했는지" 기록을 남기기 위함)</li>
   * </ul>
   *
   * <p>TODO 팀 확정 필요: 지수 삭제 시 이력도 함께 지울지(CASCADE), 지금처럼 남길지(SET_NULL).
   * ERD에 정해져 있지 않아 임시로 "남김"으로 했다. 바꾸려면 OnDeleteAction만 수정하면 된다.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private IndexInfo indexInfo;

  /**
   * 대상 날짜: 연동한 데이터의 기준 일자.
   * 지수 정보 연동(INDEX_INFO)은 특정 날짜가 없을 수 있어서 null을 허용한다.
   */
  private LocalDate targetDate;

  /**
   * 작업자.
   * 사용자가 직접 실행하면 요청한 IP 주소 (프록시 뒤에서는 X-Forwarded-For 헤더 우선),
   * 배치(스케줄러)가 실행하면 "system".
   */
  @Column(nullable = false, length = 100)
  private String worker;

  /** 작업 일시: 연동을 실행한 시각 */
  @Column(nullable = false)
  private LocalDateTime jobTime;

  /** 결과: SUCCESS(성공) 또는 FAILED(실패) */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ResultType result;


  /**
   * 새 연동 작업을 만든다.
   * sourceType 지수 정보 연동이면 INDEX_INFO,
   * O지수 데이터 연동이면 INDEX_DATA
   *
   * <p>
   * 연동 실패 (Open API 오류, 데이터 없음 등) - FAILED
   * 연동 성공 - SUCCESS
   */
  public static SyncJob create(
      JobType jobType,
      IndexInfo indexInfo,
      LocalDate targetDate,
      String worker,
      ResultType result
  ) {
    SyncJob syncJob = new SyncJob();
    syncJob.jobType = jobType;
    syncJob.indexInfo = indexInfo;
    syncJob.targetDate = targetDate;
    syncJob.worker = worker;
    syncJob.jobTime = LocalDateTime.now();
    syncJob.result = result;
    return syncJob;
  }
}
