package com.mentoring.mentoringbackend.admin;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/init")
public class AdminInitController {

    /**
     * 단순 헬스 체크 / 테스트용 엔드포인트
     * - 관리자 권한 및 라우팅이 잘 되는지 확인 용도
     */
    @PostMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("admin init endpoint is alive"));
    }

    /**
     * 초기 부트스트랩용 엔드포인트 (실제 로직은 추후 구현)
     * - 예: 기본 전공/학기/프로그램/태그 세팅
     */
    @PostMapping("/bootstrap")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> bootstrap() {
        //       초기 데이터 세팅 로직을 추후 구현할 수 있음.
        return ResponseEntity.ok(ApiResponse.success("bootstrap placeholder"));
    }
}
