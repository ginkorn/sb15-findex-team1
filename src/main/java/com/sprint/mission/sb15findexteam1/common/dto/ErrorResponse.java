package com.sprint.mission.sb15findexteam1.common.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String details
) {

}
