package com.sprint.mission.sb15findexteam1.common.dto;

import java.util.List;

public record CursorPageResponseDto<T>(
    List<T> content,
    String nextCursor,
    int nextIdAfter,
    int size,
    int totalElements,
    boolean hasNext
) {

}
