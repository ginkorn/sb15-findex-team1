package com.sprint.mission.sb15findexteam1.autosyncconfig.service;

import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigDto;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigSearchCondition;
import com.sprint.mission.sb15findexteam1.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.sb15findexteam1.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.sb15findexteam1.autosyncconfig.mapper.AutoSyncConfigMapper;
import com.sprint.mission.sb15findexteam1.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.sb15findexteam1.common.dto.CursorPageResponseDto;
import com.sprint.mission.sb15findexteam1.common.exception.BusinessException;
import com.sprint.mission.sb15findexteam1.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자동 연동 설정 서비스.
 *
 * <p>설정은 지수를 등록할 때 IndexInfoService가 함께 만든다. 여기서는 조회와 켜고 끄기만 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoSyncConfigService {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;

  /** 목록 조회 (GET /api/auto-sync-configs) */
  public CursorPageResponseDto<AutoSyncConfigDto> search(AutoSyncConfigSearchCondition condition) {
    List<AutoSyncConfigDto> rows = autoSyncConfigRepository.findPageByCondition(condition).stream()
        .map(autoSyncConfigMapper::toDto)
        .toList();
    long total = autoSyncConfigRepository.countByCondition(condition);
    return CursorPageResponseDto.of(rows, condition.size(), total,
        dto -> condition.sortByEnabled() ? dto.enabled() : dto.indexName(),
        AutoSyncConfigDto::id);
  }

  /** 켜기/끄기 (PATCH /api/auto-sync-configs/{id}) */
  @Transactional
  public AutoSyncConfigDto updateEnabled(Long id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig config = autoSyncConfigRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.AUTO_SYNC_CONFIG_NOT_FOUND, "id=" + id));
    config.changeEnabled(request.enabled());
    return autoSyncConfigMapper.toDto(config);
  }

  /** C의 배치용: 자동 연동이 켜진 지수 id 목록 */
  public List<Long> findEnabledIndexInfoIds() {
    return autoSyncConfigRepository.findEnabledIndexInfoIds();
  }
}
