package com.sprint.mission.sb15findexteam1.autosyncconfig.entity;

import com.sprint.mission.sb15findexteam1.indexinfo.entity.IndexInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * 자동 연동 설정 (DB 테이블: auto_sync_config).
 *
 * <p>지수마다 붙어 있는 "매일 자동으로 데이터 가져오기" 스위치(on/off)다.
 * C가 만드는 배치(스케줄러)는 enabled = true인 지수만 매일 연동한다.
 *
 * <p>규칙
 * <ul>
 *   <li>지수 하나당 설정은 딱 하나다. (1:1)</li>
 *   <li>지수를 등록할 때 꺼진 상태(enabled = false)로 함께 만들어진다.</li>
 *   <li>사용자가 바꿀 수 있는 값은 enabled 하나뿐이다.</li>
 *   <li>지수가 삭제되면 이 설정도 함께 삭제된다.</li>
 * </ul>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 빈 생성자. 객체는 createDisabled()로만 만든다.
@Table(name = "auto_sync_config")
public class AutoSyncConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 이 설정이 속한 지수.
   *
   * <ul>
   *   <li>{@code @OneToOne}: 지수 하나에 설정 하나</li>
   *   <li>{@code FetchType.LAZY}: 설정을 조회할 때 지수 정보는 실제로 쓸 때 가져온다. (불필요한 조회 방지)
   *       목록 조회처럼 지수명이 꼭 필요한 곳에서는 fetch join으로 한 번에 가져온다.</li>
   *   <li>{@code optional = false}, {@code nullable = false}: 지수 없는 설정은 있을 수 없다.</li>
   *   <li>{@code unique = true}: 같은 지수에 설정이 두 개 생기지 않도록 DB가 막는다.</li>
   *   <li>{@code @OnDelete(CASCADE)}: 지수가 삭제되면 DB가 이 설정도 같이 삭제한다.
   *       (없으면 "다른 데이터가 참조 중"이라며 지수 삭제가 실패한다)</li>
   * </ul>
   */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false, unique = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private IndexInfo indexInfo;

  /** 자동 연동 활성화 여부. true면 매일 배치가 이 지수의 데이터를 가져온다. */
  @Column(nullable = false)
  private boolean enabled;

  /**
   * 꺼진 상태의 자동 연동 설정을 만든다.
   *
   * <p>지수를 등록할 때(사용자 등록, Open API 등록 모두) 이 메서드로 설정을 함께 만들어야 한다.
   * 메서드 이름에 "Disabled"를 넣어 "처음엔 꺼져 있다"는 규칙이 코드에서 바로 보이게 했다.
   */
  public static AutoSyncConfig createDisabled(IndexInfo indexInfo) {
    AutoSyncConfig autoSyncConfig = new AutoSyncConfig();
    autoSyncConfig.indexInfo = indexInfo;
    autoSyncConfig.enabled = false;
    return autoSyncConfig;
  }

  /**
   * 자동 연동을 켜거나 끈다. (PATCH /api/auto-sync-configs/{id})
   *
   * @param enabled true = 켜기, false = 끄기
   */
  public void changeEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
