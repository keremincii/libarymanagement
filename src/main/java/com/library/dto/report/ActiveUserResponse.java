package com.library.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for most active users report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveUserResponse {

    private Long userId;
    private String fullName;
    private String username;
    private Long borrowCount;
}
