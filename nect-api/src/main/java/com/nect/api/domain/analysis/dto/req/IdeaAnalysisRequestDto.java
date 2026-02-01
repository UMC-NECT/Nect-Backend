
package com.nect.api.domain.analysis.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdeaAnalysisRequestDto {

    // 1. 프로젝트명
    private String projectName;

    // 2. 한줄정의
    private String projectSummary;

    // 3. 누가 이 서비스를 사용하는지?
    private String targetUsers;

    // 4. 이 서비스는 어떤 문제를 해결하는지?
    private String problemStatement;

    // 5. 서비스의 가장 중요한 핵심 3가지
    private String coreFeature1;
    private String coreFeature2;
    private String coreFeature3;

    // 6. 앱/웹 중 어떤 플랫폼인지
    private String platform;

    // 7. 참고할만한 기존 서비스나 경쟁사
    private String referenceServices;

    // 8. 구현 시 가장 걱정되거나 해결이 필요한 기술적 난관
    private String technicalChallenges;

    // 9. 생각하고 있는 프로젝트 최종 목표일
    private LocalDate targetCompletionDate;

}