package com.mentoring.mentoringbackend.admin;

import com.mentoring.mentoringbackend.assignment.repository.AssignmentRepository;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentSubmissionRepository;
import com.mentoring.mentoringbackend.feedback.repository.FeedbackRepository;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
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
}
