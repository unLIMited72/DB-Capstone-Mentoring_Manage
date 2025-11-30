package com.mentoring.mentoringbackend.admin;

import com.mentoring.mentoringbackend.assignment.domain.Assignment;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentRepository;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentSubmissionRepository;
import com.mentoring.mentoringbackend.feedback.repository.FeedbackRepository;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.session.domain.AttendanceStatus;
import com.mentoring.mentoringbackend.session.domain.SessionStatus;
import com.mentoring.mentoringbackend.session.repository.SessionAttendanceRepository;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SessionRepository sessionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final FeedbackRepository feedbackRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;       // ✅ 추가
    private final SessionAttendanceRepository sessionAttendanceRepository;   // ✅ 추가

    @Getter
    @Builder
    public static class DashboardSummary {
        private final long totalUsers;
        private final long totalPosts;
        private final long totalWorkspaces;
        private final long totalSessions;
        private final long totalAssignments;
        private final long totalSubmissions;
        private final long totalFeedbacks;
    }

    @Getter
    @Builder
    public static class KpiSummary {
        private final double matchingSuccessRate;       // 매칭 성사율
        private final double sessionCompletionRate;    // 세션 이행률
        private final double assignmentSubmissionRate; // 과제 제출률
        private final double averageAttendanceRate;    // 평균 출석률
    }

    /**
     * 단순 합계용 대시보드
     */
    public DashboardSummary getDashboardSummary() {
        return DashboardSummary.builder()
                .totalUsers(userRepository.count())
                .totalPosts(postRepository.count())
                .totalWorkspaces(workspaceRepository.count())
                .totalSessions(sessionRepository.count())
                .totalAssignments(assignmentRepository.count())
                .totalSubmissions(assignmentSubmissionRepository.count())
                .totalFeedbacks(feedbackRepository.count())
                .build();
    }

    /**
     * KPI 요약
     */
    public KpiSummary getKpiSummary() {

        // 1) 매칭 성사율 = 생성된 워크스페이스 / 모집글 수
        long totalPosts = postRepository.count();
        long totalWorkspaces = workspaceRepository.count();
        double matchingSuccessRate =
                totalPosts == 0 ? 0.0 : (double) totalWorkspaces * 100.0 / totalPosts;

        // 2) 세션 이행률 = DONE 세션 / 전체 세션
        long totalSessions = sessionRepository.count();
        long doneSessions = sessionRepository.countByStatus(SessionStatus.DONE);
        double sessionCompletionRate =
                totalSessions == 0 ? 0.0 : (double) doneSessions * 100.0 / totalSessions;

        // 3) 과제 제출률 = (실제 제출 수) / (예상 제출 수) * 100
        long totalSubmissions = assignmentSubmissionRepository.count();

        long expectedSubmissions = assignmentRepository.findAll()
                .stream()
                .mapToLong(a -> {
                    Long workspaceId = a.getWorkspace().getId();
                    // 이 워크스페이스의 멘티 수 = 이 과제의 예상 제출 수
                    return workspaceMemberRepository.countByWorkspaceIdAndRole(
                            workspaceId,
                            WorkspaceRole.MENTEE
                    );
                })
                .sum();

        double assignmentSubmissionRate =
                expectedSubmissions == 0 ? 0.0 : (double) totalSubmissions * 100.0 / expectedSubmissions;

        // 4) 평균 출석률 = PRESENT / 전체 출석 기록 * 100
        long totalAttendanceRecords = sessionAttendanceRepository.count();
        long presentCount = sessionAttendanceRepository.countByAttendanceStatus(AttendanceStatus.PRESENT);

        double averageAttendanceRate =
                totalAttendanceRecords == 0 ? 0.0 : (double) presentCount * 100.0 / totalAttendanceRecords;

        return KpiSummary.builder()
                .matchingSuccessRate(matchingSuccessRate)
                .sessionCompletionRate(sessionCompletionRate)
                .assignmentSubmissionRate(assignmentSubmissionRate)
                .averageAttendanceRate(averageAttendanceRate)
                .build();
    }
}
