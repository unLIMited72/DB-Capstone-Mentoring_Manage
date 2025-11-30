=
MENTORING-BACKEND – SYSTEM & API GUIDE (for Front-end)
============================================================

버전: v1 (초안 통합본)
작성자: (백엔드 기준 정리, 프론트엔드/운영자 공유용)
용도:
- 프론트엔드 개발자에게 “서비스 흐름 + API 명세”를 한 번에 전달
- 운영자/관리자가 시스템이 어떻게 돌아가는지 이해하는 참고 문서
- 그대로 README.md 또는 README_API_SPEC.txt 형태로 사용 가능


------------------------------------------------------------
0. 공통 규칙 (Base / Auth / Response)
------------------------------------------------------------

0-1) Base URL

- 개발/로컬:
  - http://localhost:8080/
- 서버 배포 환경:
  - http(s)://<server-domain>/

이 문서의 모든 API Path는 위 Base URL 뒤에 붙는다고 가정한다.


0-2) 인증 및 권한

- 대부분의 /api/** 엔드포인트는 JWT 기반 인증 필요
- 로그인 성공 후 받은 accessToken을 Authorization 헤더에 실어야 함

  Authorization: Bearer <accessToken>

- 권한:
  - 일반 학생: ROLE_MENTOR / ROLE_MENTEE / ROLE_BOTH 등
  - 관리자: ROLE_ADMIN
- SecurityConfig에 따라, 일부 POST/PUT/DELETE는 ADMIN만 허용


0-3) 공통 응답 구조 – ApiResponse<T>

대부분의 컨트롤러는 아래와 같은 래퍼로 응답한다.

성공 시:

{
  "success": true,
  "data": { ... 또는 [...] },
  "message": null,
  "errorCode": null
}

에러 시:

{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "errorCode": "에러코드 문자열"  // 예: "COMMON_INVALID_INPUT_VALUE"
}

※ Academic 일부 API 등은 단순 DTO / List를 바로 반환할 수도 있음
  (추후 일괄 ApiResponse<T>로 통일 가능)


0-4) 날짜/시간 포맷

- LocalDate → "YYYY-MM-DD"
  - 예) "2025-03-01"
- LocalDateTime → ISO-8601 형식
  - 예) "2025-12-01T19:00:00"

프론트에서는 문자열 그대로 사용하거나, Date 객체로 파싱해서 렌더링하면 됨.


0-5) 페이징 응답 – PageResponse<T>

목록 API 중 일부는 PageResponse<T>를 사용:

{
  "content": [ ... DTO 배열 ... ],
  "page": 0,
  "size": 20,
  "totalElements": 124,
  "totalPages": 7,
  "last": false
}

- page, size: 요청 시 넘긴 값
- totalElements, totalPages: 전체 개수/페이지 수
- last: 마지막 페이지 여부


=
1.운영파이프라인(0 ~ 8)
============================================================


이 섹션은 “현실에서 이 시스템이 어떻게 사용되는지”를 스토리 형태로 정리한 것.
각 단계에서 어떤 API를 쓰는지는 뒤에서 API 명세에 매핑된다.


------------------------------------------------------------
1-0. 초기 세팅 (관리자 관점)
------------------------------------------------------------

서비스를 처음 도입할 때, 가장 먼저 해야 할 일은 관리자 계정을 만드는 것이다.
관리자 계정은 일반 멘토·멘티 계정과 달리,

- 전공(학과) 목록
- 학기(Semester)
- 프로그램(Program)
- 기본 태그(Tag)

를 등록·수정할 수 있는 권한을 가진다.

1) 전공 정보 정의
- 예: “항공전자공학과”, “항공소프트웨어공학과”
- 학생들이 회원 가입 시 자신의 전공을 선택할 때 참조
- API: /api/academic/majors (GET/POST)

2) 태그(Tag) 정의
- 예: “C 언어”, “RTOS”, “임베디드”, “항전기초” 등
- 시스템에서 미리 넣어두는 태그들은
  - is_system = true
  - is_matchable = true
  로 설정한다.
- 자동 매칭 알고리즘이 특정 태그들을 기준으로 유사도를 계산할 수 있게 하기 위함.
- API: /api/tags (POST), /api/tags/matchable (GET) 등

3) 학기(Semester) 및 프로그램(Program) 설정
- 예:
  - 학기: "2025-1"
  - 프로그램:
    - "한서튜터링"
    - "항전 학습공동체"
- 이후 생성되는 모집글(Post), 매칭, 세션(Session), 과제(Assignment)는
  모두 특정 학기와 특정 프로그램에 속하게 된다.
- API:
  - /api/academic/semesters
  - /api/academic/programs


------------------------------------------------------------
1-1. 회원 가입과 프로필 설정 (멘토·멘티 공통)
------------------------------------------------------------

학생이 서비스를 사용하기 위해 가장 먼저 하는 일은 회원 가입이다.

1) 회원 가입
- 화면에서 제출하는 정보:
  - 이메일(email)
  - 비밀번호(password)
  - 이름(name)
  - 학번(studentId)
  - 전공(majorId)
  - 역할(role: MENTOR / MENTEE / BOTH / ADMIN 등)
- user 테이블에 저장되며, majorId는 앞서 정의된 전공 목록 참조
- API: POST /auth/signup

2) 로그인
- 이메일/비밀번호로 로그인
- API: POST /auth/login
- 성공 시:
  - accessToken, tokenType(Bearer) 반환
  - 프론트는 accessToken을 저장 후, 이후 모든 /api/** 호출 시 Authorization 헤더에 포함

3) 프로필 상세 설정
- 관심사/역량/학습 목표를 태그 형태로 입력
  - 시스템에 미리 정의된 태그 선택 + 필요 시 유저 커스텀 태그 추가
  - 각 태그의 역할:
    - CAN_TEACH (가르칠 수 있는 것)
    - WANT_TO_LEARN (배우고 싶은 것)
    - INTEREST (관심 분야)
  - 숙련도: level (1~5)
- API:
  - /api/users/me/tags (GET / PUT)
  - /api/tags (GET, 필요 시 커스텀 생성)

4) 가용 시간 및 선호 방식 설정
- 요일/시간 기준 “언제 멘토링 가능한지” 입력
  - 예: 수·목 18:00~21:00, ONLINE 선호
- 이후 자동 매칭에서 멘토/멘티의 시간대 겹침을 계산하는 데 사용
- API:
  - /api/users/me/availability (GET / PUT)

이 단계까지 완료되면:
- 전공
- 역할(멘토/멘티/둘다)
- 관심/역량/목표 태그
- 가용 시간/선호 방식

등이 모두 데이터로 축적되고, 이후 매칭 단계에서 적극적으로 활용된다.


------------------------------------------------------------
1-2. 모집글과 요청글 작성 (멘토·멘티의 공개 게시)
------------------------------------------------------------

프로필 설정 후, 멘토와 멘티는 각자의 필요에 따라 모집글 또는 요청글을 작성한다.

1) 멘토 모집글 (MENTOR_RECRUIT)
- 예: "C 언어 기초 튜터링 멘티 3명 모집"
- 포함 정보:
  - 어떤 주제를 다룰지
  - 학습 목표
  - 대상 수준 (예: 1~2학년, 기초 수준)
  - 예상 운영 기간 및 회차 수
  - 진행 방식 (온라인/오프라인/혼합)
  - 대략적인 시간대
  - 연관 태그 리스트
- post 테이블에 type='MENTOR_RECRUIT'로 저장
- API: POST /api/posts

2) 멘티 요청글 (MENTEE_REQUEST)
- 예: "RTOS 기반 임베디드 공부할 멘토를 찾습니다"
- 포함 정보:
  - 본인이 공부하고 싶은 내용
  - 현재 수준
  - 목표
  - 희망 방식/시간대
- post 테이블에 type='MENTEE_REQUEST'로 저장
- API: POST /api/posts

3) 게시글 탐색
- 프로그램, 전공, 태그, 상태(OPEN/MATCHED/CLOSED), 타입 등으로 필터링
- API:
  - GET /api/posts
  - GET /api/posts/{postId}


------------------------------------------------------------
1-3. 매칭 – 수동 매칭과 자동 매칭
------------------------------------------------------------

1-3-1. 수동 매칭 (양방향 신청)

1) 멘티 → 멘토 신청
- 멘티가 멘토 모집글을 보고 “신청하기” 클릭
- post_application 레코드 생성:
  - post_id: 대상 모집글
  - from_user_id: 신청자(멘티)
  - to_user_id: 글 작성자(멘토)
  - status: PENDING
- API:
  - POST /api/post-applications

2) 멘토 → 멘티 신청
- 멘토가 멘티 요청글을 보고 “신청하기” 클릭
- post_application 생성:
  - from_user_id: 멘토
  - to_user_id: 멘티
- 나머지는 동일

3) 신청 관리
- “내가 보낸 신청”, “내가 받은 신청” 화면
- API:
  - GET /api/post-applications/me/sent
  - GET /api/post-applications/me/received

4) 수락/거절
- 신청을 받은 당사자만 해당 신청을 수락/거절 가능
- 수락 시:
  - post_application.status = ACCEPTED
  - 정원 체크 후 post.status를 MATCHED/CLOSED로 갱신
  - 워크스페이스 생성 또는 기존 워크스페이스에 참여자로 추가
- 거절 시:
  - status = REJECTED
- API:
  - POST /api/post-applications/{applicationId}/accept
  - POST /api/post-applications/{applicationId}/reject


1-3-2. 자동 매칭 (추천 기능)

1) 추천 요청
- 멘티가 “추천 멘토 보기” 버튼 클릭
- API:
  - GET /api/programs/{programId}/matching/recommendations

2) 후보 선정
- 조건:
  - 같은 Program
  - post.type = MENTOR_RECRUIT
  - post.status = OPEN
  - 정원 남아 있음

3) 점수 계산 요소 예시
- 태그 유사도
  - 멘티의 WANT_TO_LEARN/INTEREST vs 멘토의 CAN_TEACH
  - 게시글에 달린 태그 vs 멘티 태그
- 시간대 겹침
  - 멘토와 멘티 UserAvailability 비교
- 전공/학년/분야 적합성

4) 가중치 및 필터
- matching_config에 weightTag, weightTime, minScore 저장
- 점수 = 태그 점수 x weightTag + 시간 점수 x weightTime …
- minScore 이상인 후보만 남기고 상위 N개 반환

5) UI
- 멘티는 추천 목록에서 원하는 멘토를 선택해 “신청하기”
- 내부적으로는 수동 매칭과 동일하게 post_application 생성


------------------------------------------------------------
1-4. 워크스페이스와 연락처 공유
------------------------------------------------------------

1) 워크스페이스 생성 조건
- post_application.status가 ACCEPTED로 변경되는 시점
- WorkspaceService.createWorkspaceFromAcceptedApplication(...) 호출
- 같은 program + sourcePost에 대해 ACTIVE 워크스페이스가 있으면 재사용,
  없으면 새 워크스페이스 생성

2) 워크스페이스 정보
- 어떤 Program에 속하는지
- 어떤 모집글에서 파생되었는지 (sourcePostId)
- 멘토/멘티 구성
- 시작/종료일, 상태(WorkspaceStatus: ACTIVE / FINISHED 등)

3) 워크스페이스 멤버
- workspace_member:
  - user_id
  - workspace_id
  - role (MENTOR / MENTEE)
  - joined_at 등

4) 연락처 공유
- 신청 단계에서는 서로의 연락처를 볼 수 없음
- 워크스페이스가 생성된 후에만 contact_info를 통해 연락처 일부 노출
  - 타입: KAKAO, DISCORD, EMAIL, PHONE, OTHER
  - visibleToWorkspaceMembers = true인 항목만 공유
- 실제 연락은 카카오톡/이메일 등 외부에서 진행되지만,
  시스템 UI에서 연락처를 확인하는 창구를 제공

5) 주요 API
- 워크스페이스 생성/조회:
  - POST /api/workspaces (수동, 관리자용)
  - GET /api/workspaces/{workspaceId}
  - GET /api/workspaces/me


------------------------------------------------------------
1-5. 세션(회차) 계획·운영·기록
------------------------------------------------------------

워크스페이스가 만들어졌다면, 그 안에서 개별 세션(회차)을 계획하고 기록한다.

1) 세션 계획
- 주차(weekIndex), 주제(topic), 예정일시(scheduledAt), 모드(mode), 계획(plan) 등 입력
- 예:
  - 1주차: C 언어 변수/자료형
  - 2주차: 제어문
  - ...
- 세션 정보는 session 테이블에 저장
- API:
  - POST /api/workspaces/{workspaceId}/sessions
  - GET /api/workspaces/{workspaceId}/sessions

2) 세션 상태 관리
- status:
  - PLANNED: 예정
  - DONE: 완료
  - CANCELED: 취소
- 세션이 정상적으로 끝나면 DONE으로 변경
- API:
  - PUT /api/workspaces/{workspaceId}/sessions/{sessionId}

3) 출석 기록
- 세션 진행 후, 멘토/운영자는 출석을 기록
- session_attendance:
  - userId
  - sessionId
  - attendanceStatus: PRESENT / ABSENT / LATE / EXCUSED 등
- 출석률, 지각/결석 통계를 나중에 KPI에서 사용
- API:
  - GET /api/sessions/{sessionId}/attendance
  - POST /api/sessions/{sessionId}/attendance

4) 세션 노트/과제 요약
- session.note: 세션에서 무엇을 했는지 기록
- session.homeworkSummary: 해당 세션에서 부여된 과제 요약
- 나중에 “전체 활동 로그”를 복기하는 데 도움


------------------------------------------------------------
1-6. 과제 관리, 제출, 피드백
------------------------------------------------------------

1) 과제 생성
- 멘토는 각 워크스페이스 내에서 과제를 생성
- 특정 세션에 귀속될 수도 있고, 전체 워크스페이스 대상으로도 가능
- assignment:
  - workspaceId
  - sessionId (optional)
  - title, description
  - dueDate (마감 시간)
  - createdById 등
- API:
  - POST /api/workspaces/{workspaceId}/assignments
  - GET  /api/workspaces/{workspaceId}/assignments
  - GET  /api/workspaces/{workspaceId}/assignments/{assignmentId}
  - PUT  /api/workspaces/{workspaceId}/assignments/{assignmentId}

2) 과제 제출
- 멘티는 과제 제출 화면에서 자신의 답안을 입력/수정
- assignment_submission:
  - assignmentId
  - userId
  - content
  - submittedAt
  - status (SUBMITTED / MISSING 등)
- API:
  - GET  /api/assignments/{assignmentId}/submissions
  - POST /api/assignments/{assignmentId}/submissions

3) 피드백 및 채점
- 멘토는 각 제출물에 대해 feedback, score를 기록
- 상태를 GRADED 등으로 관리할 수 있음 (구현에 따라)
- 동일 API(/submissions)로 멘티 제출 + 멘토 피드백을 처리
  - 멘티:
    - 자기 userId에 대해서 content만 제출/수정 가능
  - 멘토:
    - mentee userId에 대해 feedback/score 입력 가능


------------------------------------------------------------
1-7. 상호 피드백과 만족도 기록
------------------------------------------------------------

1) 피드백 대상
- PROGRAM: 전체 프로그램/워크스페이스에 대한 평가
- MENTOR: 특정 멘토에 대한 평가
- MENTEE: 특정 멘티에 대한 평가

2) 피드백 내용
- rating: 1~5 점수
- comment: 자유 코멘트
- anonymous: 익명 여부

3) 저장 구조
- feedback:
  - workspaceId
  - sessionId (optional)
  - targetType (PROGRAM/MENTOR/MENTEE)
  - fromUserId (익명 시 null)
  - toUserId (PROGRAM이면 null)
  - rating, comment, anonymous
  - createdAt, updatedAt

4) 활용
- 멘토별 평균 만족도
- 프로그램별 만족도 추이
- 향후 매칭 시, 만족도가 높은 멘토를 우선 추천하는 데 활용 가능

5) API:
- POST /api/workspaces/{workspaceId}/feedbacks
- GET  /api/workspaces/{workspaceId}/feedbacks[?sessionId=]


------------------------------------------------------------
1-8. 대시보드와 KPI(성과 지표)
------------------------------------------------------------

관리자/운영자는 대시보드를 통해 전체 프로그램 운영 상태를 확인한다.

1) 기본 통계
- totalUsers
- totalPosts
- totalWorkspaces
- totalSessions
- totalAssignments
- totalSubmissions
- totalFeedbacks
- API: GET /api/admin/dashboard

2) 핵심 KPI
- matchingSuccessRate (%)
  - 실제 워크스페이스로 이어진 매칭 / 전체 모집글 비율
- sessionCompletionRate (%)
  - DONE 세션 / 전체 세션
- assignmentSubmissionRate (%)
  - 실제 제출 수 / 예상 제출 수
- averageAttendanceRate (%)
  - PRESENT 출석 / 전체 attendance 기록
- API: GET /api/admin/dashboard/kpi

3) 시각화 예시
- 프로그램별 워크스페이스 수, 세션 수, 출석률, 과제 제출률
- 학기별 활동량 추이
- 만족도 평균 그래프

이러한 대시보드와 KPI는 단순 기록을 넘어, 프로그램의 투명성과 신뢰도를 높이고,
다음 학기 운영 전략을 세우는 기준 자료가 된다.


=
2. 프론트엔드용 API 요약 (도메인별)
============================================================

이 섹션은 실제로 프론트에서 사용할 API들을 엔드포인트 기준으로 정리한 부분이다.
(요청/응답 DTO의 세부 필드는 코드 상 DTO 정의와 함께 참고)


------------------------------------------------------------
2-0. 공통 규칙 (요약, 재정리)
------------------------------------------------------------

- Base URL: http(s)://<server>/
- 대부분 API: JWT 필요
  - Authorization: Bearer <accessToken>
- 응답:
  - 대부분 ApiResponse<T> 래핑
  - 일부 Academic 등은 DTO / List 직접 반환
- ApiResponse<T>:

  {
    "success": true,
    "data": { ... },
    "message": null,
    "errorCode": null
  }

- 에러 시:

  {
    "success": false,
    "data": null,
    "message": "에러 메시지",
    "errorCode": "에러코드 문자열"
  }


------------------------------------------------------------
2-1. 인증 / 사용자 진입점
------------------------------------------------------------

2-1-1. 회원가입

- Method: POST
- Path  : /auth/signup
- Body  : UserSignupRequest

예시 Body:

{
  "email": "user@example.com",
  "password": "pass1234",
  "name": "홍길동",
  "studentId": "20251234",
  "majorId": 1,
  "role": "MENTEE"
}

- Response: ApiResponse<UserProfileResponse>


2-1-2. 로그인

- Method: POST
- Path  : /auth/login
- Body  : UserLoginRequest

{
  "email": "user@example.com",
  "password": "pass1234"
}

- Response: ApiResponse<LoginResponse>

예시:

{
  "success": true,
  "data": {
    "accessToken": "<JWT_TOKEN>",
    "tokenType": "Bearer"
  },
  "message": null,
  "errorCode": null
}

프론트:
- accessToken 저장 후
- 이후 모든 /api/** 호출 시 Authorization 헤더에 Bearer 토큰 추가


------------------------------------------------------------
2-2. Academic 도메인 (전공/학기/프로그램)
------------------------------------------------------------

2-2-1. 전공 목록 조회

- Method: GET
- Path  : /api/academic/majors
- Auth  : authenticated
- Response: List<MajorDto>

MajorDto:

{
  "id": 1,
  "name": "항공전자공학과"
}


2-2-2. 전공 생성 (관리자)

- Method: POST
- Path  : /api/academic/majors
- Auth  : ROLE_ADMIN
- Body  : MajorDto (name 필수)
- Response: MajorDto


2-2-3. 학기 목록 조회

- Method: GET
- Path  : /api/academic/semesters
- Query:
  - activeOnly=true|false
- Response: List<SemesterDto>

SemesterDto:

{
  "id": 1,
  "name": "2025-1",
  "startDate": "2025-03-01",
  "endDate": "2025-06-30",
  "isActive": true
}


2-2-4. 학기 생성 (관리자)

- Method: POST
- Path  : /api/academic/semesters
- Body  : SemesterDto (name, startDate, endDate, isActive)
- Response: SemesterDto


2-2-5. 프로그램 목록 조회

- Method: GET
- Path  : /api/academic/programs
- Query:
  - semesterId (optional)
  - activeOnly (default: false)
- Response: List<ProgramDto>

ProgramDto:

{
  "id": 1,
  "semesterId": 1,
  "name": "한서튜터링",
  "type": "TUTORING",   // TUTORING | LEARNING_COMMUNITY | CAPSTONE | EXTERNAL_ACTIVITY
  "isActive": true
}


2-2-6. 프로그램 생성 (관리자)

- Method: POST
- Path  : /api/academic/programs
- Body  : ProgramDto (semesterId, name, type, isActive)
- Response: ProgramDto


------------------------------------------------------------
2-3. Tag(태그) 관련 API
------------------------------------------------------------

2-3-1. 기본 태그 생성 (관리자/초기 세팅용)

- Method: POST
- Path  : /api/tags
- Body  : TagCreateRequest

예시:

{
  "name": "전공 기초 다지기",
  "type": "GOAL",       // INTEREST | SKILL | GOAL
  "system": true,
  "matchable": true,
  "parentTagId": null,
  "description": "멘토링에서 전공 기초를 다지는 목표"
}

- Response: TagDto


2-3-2. 사용자 커스텀 태그 생성

- Method: POST
- Path  : /api/tags/custom
- Body  : TagCreateRequest
  - 서버에서 system=false, matchable=false로 강제 설정
- Response: TagDto


2-3-3. 전체 태그 조회

- Method: GET
- Path  : /api/tags
- Response: List<TagDto>


2-3-4. 타입별 태그 조회

- Method: GET
- Path  : /api/tags/type/{type}
- PathVar:
  - type = INTEREST | SKILL | GOAL
- Response: List<TagDto>


2-3-5. 매칭에 사용 가능한 태그 조회

- Method: GET
- Path  : /api/tags/matchable
- Response: List<TagDto>


2-3-6. TagDto 구조

{
  "id": 1,
  "name": "전공 기초 다지기",
  "type": "GOAL",
  "system": true,
  "matchable": true,
  "parentTagId": null,
  "parentTagName": null,
  "description": null
}


------------------------------------------------------------
2-4. User(프로필 / 태그 / 가용 시간)
------------------------------------------------------------

2-4-1. 내 프로필 조회

- Method: GET
- Path  : /api/users/me   (또는 /api/profile/me – 기능 유사)
- Response: UserProfileResponse

예시:

{
  "id": 3,
  "email": "student@hanseo.ac.kr",
  "name": "홍길동",
  "studentId": "20251234",
  "majorId": 1,
  "majorName": "항공전자공학과",
  "role": "MENTOR",   // MENTOR | MENTEE | BOTH | ADMIN
  "active": true
}


2-4-2. 사용자 목록 조회 (관리자용)

- Method: GET
- Path  : /api/users
- Query:
  - page, size
- Response: PageResponse<UserProfileResponse>


2-4-3. 내 가용 시간 조회

- Method: GET
- Path  : /api/users/me/availability
- Response: List<UserAvailabilityResponse>

예시:

[
  {
    "id": 1,
    "dayOfWeek": 1,      // 0=Sun ~ 6=Sat
    "startTime": "18:00:00",
    "endTime": "21:00:00",
    "mode": "ONLINE"     // ONLINE | OFFLINE | HYBRID
  }
]


2-4-4. 내 가용 시간 전체 업데이트(덮어쓰기)

- Method: PUT
- Path  : /api/users/me/availability
- Body  : List<UserAvailabilityRequest>

예시:

[
  {
    "dayOfWeek": 1,
    "startTime": "18:00:00",
    "endTime": "21:00:00",
    "mode": "ONLINE"
  },
  {
    "dayOfWeek": 3,
    "startTime": "19:00:00",
    "endTime": "22:00:00",
    "mode": "OFFLINE"
  }
]

기존 레코드 전체 삭제 후 새로 생성.


2-4-5. 내 태그(관심/역량/목표) 조회

- Method: GET
- Path  : /api/users/me/tags
- Response: List<UserTagResponse>

예시:

[
  {
    "id": 10,
    "tagId": 2,
    "tagName": "전공 기초 다지기",
    "tagType": "GOAL",
    "relationType": "WANT_TO_LEARN",   // CAN_TEACH | WANT_TO_LEARN | INTEREST
    "level": 4                          // 1~5
  }
]


2-4-6. 내 태그 전체 업데이트(덮어쓰기)

- Method: PUT
- Path  : /api/users/me/tags
- Body  : List<UserTagRequest>

예시:

[
  {
    "tagId": 2,
    "relationType": "WANT_TO_LEARN",
    "level": 4
  },
  {
    "tagId": 5,
    "relationType": "INTEREST",
    "level": 5
  }
]


2-4-7. 연락처(ContactInfo)

- 서비스 계층에 구현:
  - UserService.updateMyContactInfos(...)
  - UserService.getMyContactInfos()
- 전역 프로필용 연락처 (workspace=null)
- contact 타입: KAKAO, DISCORD, EMAIL, PHONE, OTHER
- visibleToWorkspaceMembers: 워크스페이스 멤버에게 공개 여부
- 추후 /api/users/me/contacts 컨트롤러 추가 예정


------------------------------------------------------------
2-5. Post(게시글) & PostApplication(신청)
------------------------------------------------------------

2-5-1. 게시글 생성

- Method: POST
- Path  : /api/posts
- Body  : PostCreateRequest

예시:

{
  "programId": 1,
  "type": "MENTOR_RECRUIT",       // MENTOR_RECRUIT | MENTEE_REQUEST
  "title": "C언어 기초 멘토링",
  "content": "매주 1회, C언어 기초 복습 스터디입니다.",
  "targetLevel": "1~2학년",
  "maxMembers": 3,
  "expectedWeeks": 8,
  "expectedSessionsTotal": 8,
  "expectedSessionsPerWeek": 1,
  "preferredMode": "ONLINE",
  "preferredTimeNote": "평일 저녁",
  "tagIds": [1, 2, 5]
}

- Response: PostResponse


2-5-2. 게시글 단일 조회

- Method: GET
- Path  : /api/posts/{postId}
- Response: PostResponse

예시:

{
  "id": 10,
  "programId": 1,
  "programName": "한서튜터링",
  "authorId": 3,
  "authorName": "홍길동",
  "type": "MENTOR_RECRUIT",
  "status": "OPEN",  // OPEN | MATCHED | CLOSED
  "title": "...",
  "content": "...",
  "targetLevel": "1~2학년",
  "maxMembers": 3,
  "expectedWeeks": 8,
  "expectedSessionsTotal": 8,
  "expectedSessionsPerWeek": 1,
  "preferredMode": "ONLINE",
  "preferredTimeNote": "평일 저녁",
  "createdAt": "...",
  "updatedAt": "...",
  "tags": [ TagDto... ]
}


2-5-3. 게시글 목록 조회(필터 + 페이징)

- Method: GET
- Path  : /api/posts
- Query:
  - programId (optional)
  - type (MENTOR_RECRUIT / MENTEE_REQUEST)
  - status (OPEN / MATCHED / CLOSED)
  - page (default 0)
  - size (default 20)
- Response: PageResponse<PostResponse>


2-5-4. 게시글 수정

- Method: PUT
- Path  : /api/posts/{postId}
- Body  : PostUpdateRequest
  - programId, type 제외하고 대부분 PostCreateRequest와 유사
- 조건:
  - 작성자 본인만 수정 가능
- 태그 수정 정책:
  - tagIds가 null이 아니면:
    - 기존 태그와 비교해 제거/유지/추가 계산
  - tagIds가 null이면:
    - 태그 유지 (혹은 구현에 따라 정책 조정)


2-5-5. 게시글 삭제 (Soft Delete)

- Method: DELETE
- Path  : /api/posts/{postId}
- 조건:
  - 작성자 본인
  - status != MATCHED (이미 매칭된 글 삭제 불가)
- 처리:
  - 실제 DB 삭제 대신 status = CLOSED


2-5-6. 신청 생성 (PostApplication)

- Method: POST
- Path  : /api/post-applications
- Body  : PostApplicationRequest

예시:

{
  "postId": 10,
  "message": "멘티로 참여하고 싶습니다!"
}

제약:
- 자기 글에는 신청 불가
- post.status != CLOSED
- 동일 (post, fromUser, toUser) 조합의 PENDING 신청이 존재하면 중복 신청 불가

- Response: PostApplicationResponse (status=PENDING)


2-5-7. 내가 보낸 신청 목록

- Method: GET
- Path  : /api/post-applications/me/sent
- Response: List<PostApplicationResponse>


2-5-8. 내가 받은 신청 목록

- Method: GET
- Path  : /api/post-applications/me/received
- Response: List<PostApplicationResponse>


2-5-9. 신청 수락

- Method: POST
- Path  : /api/post-applications/{applicationId}/accept
- 조건:
  - 신청의 toUser == 현재 로그인 유저
  - status == PENDING
- 처리:
  - application.status = ACCEPTED
  - 정원 체크 후 post.status = MATCHED 또는 CLOSED
  - WorkspaceService.createWorkspaceFromAcceptedApplication(...) 호출


2-5-10. 신청 거절

- Method: POST
- Path  : /api/post-applications/{applicationId}/reject
- 처리:
  - application.status = REJECTED


------------------------------------------------------------
2-6. Matching(자동 매칭 추천)
------------------------------------------------------------

2-6-1. 추천 목록 조회

- Method: GET
- Path  : /api/programs/{programId}/matching/recommendations
- 설명:
  - 현재 로그인 유저를 멘티로 보고 적합한 멘토 모집글 추천
- 후보:
  - 같은 programId
  - type = MENTOR_RECRUIT
  - status = OPEN
  - 정원 남음
- Response: List<MatchingSuggestionResponse>

예시:

[
  {
    "postId": 10,
    "title": "C언어 기초 멘토링",
    "type": "MENTOR_RECRUIT",
    "mentorId": 3,
    "mentorName": "홍길동",
    "score": 0.82
  },
  ...
]


2-6-2. 매칭 가중치 설정 조회

- Method: GET
- Path  : /api/programs/{programId}/matching/config
- Response: MatchingConfigRequest

예시:

{
  "weightTag": 0.7,
  "weightTime": 0.3,
  "minScore": 0.3
}


2-6-3. 매칭 가중치 설정 수정 (관리자용)

- Method: PUT
- Path  : /api/programs/{programId}/matching/config
- Body  : MatchingConfigRequest

{
  "weightTag": 0.5,
  "weightTime": 0.5,
  "minScore": 0.4
}

- 합계가 1이 아니어도 되며, 내부 로직에서 normalize할 수 있음.


------------------------------------------------------------
2-7. Workspace(워크스페이스)
------------------------------------------------------------

WorkspaceController:

- Base: /api/workspaces

2-7-1. 워크스페이스 수동 생성 (관리자/운영자용)

- Method: POST
- Path  : /api/workspaces
- Body  : WorkspaceCreateRequest
- Auth  : 관리자/운영자 권한

예시 형태(실제 필드는 DTO 참고):

{
  "programId": 1,
  "sourcePostId": 10,
  "mentorId": 3,
  "menteeId": 7
}

- Response: ApiResponse<WorkspaceDetailResponse>


2-7-2. 워크스페이스 상세 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}
- Response: ApiResponse<WorkspaceDetailResponse>

예시:

{
  "success": true,
  "data": {
    "workspaceId": 15,
    "programId": 1,
    "programName": "2025-1 항공전자 멘토링",
    "status": "ACTIVE",
    "mentor": {
      "userId": 3,
      "name": "홍길동"
    },
    "mentee": {
      "userId": 7,
      "name": "김학생"
    },
    "sourcePostId": 10,
    "createdAt": "2025-11-30T12:34:56",
    "updatedAt": "2025-11-30T12:34:56"
    // 세부 필드는 WorkspaceDetailResponse 참고
  }
}


2-7-3. 내가 속한 워크스페이스 목록 조회

- Method: GET
- Path  : /api/workspaces/me
- Response: ApiResponse<List<WorkspaceSummaryResponse>>

예시:

{
  "success": true,
  "data": [
    {
      "workspaceId": 15,
      "programId": 1,
      "programName": "2025-1 항공전자 멘토링",
      "roleInWorkspace": "MENTEE",
      "status": "ACTIVE",
      "lastUpdatedAt": "2025-11-30T12:34:56"
    },
    {
      "workspaceId": 18,
      "programId": 2,
      "programName": "2025-1 학습공동체",
      "roleInWorkspace": "MENTOR",
      "status": "ACTIVE",
      "lastUpdatedAt": "2025-11-29T09:20:10"
    }
  ]
}


------------------------------------------------------------
2-8. Session(회차) & Attendance(출석)
------------------------------------------------------------

Base Path: /api/workspaces/{workspaceId}/sessions

2-8-1. 세션 생성 (멘토)

- Method: POST
- Path  : /api/workspaces/{workspaceId}/sessions
- Body  : SessionCreateRequest

예시:

{
  "weekIndex": 1,
  "topic": "C언어 기본 문법",
  "scheduledAt": "2025-03-05T19:00:00",
  "mode": "ONLINE",      // ONLINE | OFFLINE | HYBRID
  "plan": "이론 30분 + 실습 30분",
  "note": null,
  "homeworkSummary": null
}

- 제약:
  - 현재 유저가 해당 워크스페이스의 MENTOR
  - 워크스페이스 상태가 FINISHED면 생성 불가


2-8-2. 세션 목록 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}/sessions
- Response: List<SessionResponse>
- 워크스페이스 멤버(멘토/멘티) 모두 조회 가능


2-8-3. 세션 상세 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}/sessions/{sessionId}
- Response: SessionResponse


2-8-4. 세션 수정 (멘토)

- Method: PUT
- Path  : /api/workspaces/{workspaceId}/sessions/{sessionId}
- Body  : SessionUpdateRequest

예시:

{
  "weekIndex": 2,
  "topic": "포인터 기초",
  "scheduledAt": "2025-03-12T19:00:00",
  "mode": "ONLINE",
  "status": "PLANNED",      // PLANNED | DONE | CANCELED
  "plan": "...",
  "note": "...",
  "homeworkSummary": "과제 내용"
}


2-8-5. 출석 목록 조회

- Method: GET
- Path  : /api/sessions/{sessionId}/attendance
- Response: List<SessionAttendanceResponse>

예시:

[
  {
    "userId": 3,
    "userName": "멘티김",
    "attendanceStatus": "PRESENT",    // PRESENT | ABSENT | LATE | EXCUSED
    "checkedAt": "2025-03-05T19:10:00"
  }
]


2-8-6. 출석 기록/수정 (멘토)

- Method: POST
- Path  : /api/sessions/{sessionId}/attendance
- Body  : SessionAttendanceRequest

예시:

{
  "userId": 4,
  "attendanceStatus": "PRESENT"
}

- 제약:
  - 현재 유저가 해당 워크스페이스의 MENTOR
  - 세션 status != CANCELED
  - userId는 워크스페이스 멤버여야 함


------------------------------------------------------------
2-9. Assignment(과제) & AssignmentSubmission(제출)
------------------------------------------------------------

Base Path: /api/workspaces/{workspaceId}/assignments

2-9-1. 과제 생성 (멘토)

- Method: POST
- Path  : /api/workspaces/{workspaceId}/assignments
- Body  : AssignmentCreateRequest

예시:

{
  "sessionId": 1,               // optional
  "title": "포인터 연습 문제",
  "description": "포인터, 배열 관련 문제 5개를 풀어오세요.",
  "dueDate": "2025-12-01T23:59:00"
}

- Response: ApiResponse<AssignmentResponse>


2-9-2. 워크스페이스 내 과제 목록 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}/assignments
- Response: ApiResponse<List<AssignmentResponse>>


2-9-3. 단일 과제 상세 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}/assignments/{assignmentId}
- Response: ApiResponse<AssignmentResponse>


2-9-4. 과제 수정 (멘토)

- Method: PUT
- Path  : /api/workspaces/{workspaceId}/assignments/{assignmentId}
- Body  : AssignmentCreateRequest (동일 구조)
- Response: ApiResponse<AssignmentResponse>


2-9-5. 제출 목록 조회

- Method: GET
- Path  : /api/assignments/{assignmentId}/submissions
- 권한:
  - 워크스페이스 멤버만
  - 멘토: 해당 과제의 모든 제출 조회
  - 멘티: 자신의 제출만 조회
- Response: ApiResponse<List<AssignmentSubmissionResponse>>

예시:

[
  {
    "id": 1,
    "assignmentId": 10,
    "userId": 7,
    "userName": "멘티김",
    "content": "깃허브 링크 or 내용",
    "submittedAt": "2025-03-10T21:00:00",
    "status": "SUBMITTED",     // SUBMITTED | MISSING 등
    "feedback": "좋습니다. 다음엔 포인터 연산도 더 연습해보세요.",
    "score": 95,
    "createdAt": "...",
    "updatedAt": "..."
  }
]


2-9-6. 제출/수정 + 피드백/채점

- Method: POST
- Path  : /api/assignments/{assignmentId}/submissions
- Body  : AssignmentSubmissionRequest

{
  "userId": 7,              // 대상 유저 (멘티 or 멘토)
  "content": "제출 내용",
  "feedback": "멘토 코멘트",
  "score": 95
}

권한 및 동작:
- 멘티:
  - userId == 본인일 때만
  - content만 입력/수정 가능
  - feedback, score는 무시
- 멘토:
  - 어떤 mentee userId에 대해서도 feedback/score 입력 가능
- 제약:
  - content, feedback, score가 모두 null이면 400 에러
    - errorCode: COMMON_INVALID_INPUT_VALUE
    - message: "제출 내용 또는 피드백/점수 중 하나는 있어야 합니다."

- Response: ApiResponse<AssignmentSubmissionResponse>


------------------------------------------------------------
2-10. Feedback(피드백)
------------------------------------------------------------

Base Path: /api/workspaces/{workspaceId}/feedbacks

2-10-1. 피드백 작성

- Method: POST
- Path  : /api/workspaces/{workspaceId}/feedbacks
- Body  : FeedbackRequest

예시:

{
  "sessionId": 1,                 // 특정 세션 기반 평가면 ID, 아니면 null
  "toUserId": 3,                  // PROGRAM일 때는 null, MENTOR/MENTEE라면 필수
  "targetType": "MENTOR",         // PROGRAM | MENTOR | MENTEE
  "rating": 5,                    // 1~5
  "comment": "설명이 이해하기 쉬웠습니다.",
  "anonymous": true
}

제약:
- rating: 1~5
- targetType == PROGRAM 이면 toUserId는 null이어야 함
- targetType == MENTOR/MENTEE 이면 toUserId 필수
- toUserId는 워크스페이스 멤버여야 함
- targetType == MENTOR → 해당 유저가 MENTOR
- targetType == MENTEE → 해당 유저가 MENTEE

- Response: FeedbackResponse


2-10-2. 피드백 조회

- Method: GET
- Path  : /api/workspaces/{workspaceId}/feedbacks
- Query:
  - sessionId (optional)
- Response: List<FeedbackResponse>


------------------------------------------------------------
2-11. Notification(알림)
------------------------------------------------------------

Base Path: /api/notifications

2-11-1. 내 알림 목록 조회

- Method: GET
- Path  : /api/notifications
- Response: List<NotificationResponse>

예시:

[
  {
    "id": 1,
    "title": "세션 D-1 리마인더",
    "message": "내일 19시에 C언어 멘토링 세션이 예정되어 있습니다.",
    "type": "SESSION_REMINDER",     // SYSTEM | SESSION_REMINDER | ASSIGNMENT_DUE
    "status": "SENT",               // PENDING | SENT | READ
    "scheduledAt": "2025-03-04T19:00:00",
    "sentAt": "2025-03-03T19:00:00",
    "createdAt": "2025-03-01T12:00:00"
  }
]


2-11-2. 알림 읽음 처리

- Method: POST
- Path  : /api/notifications/{id}/read
- Response: ApiResponse<Void> (data=null)
- 현재 로그인 유저의 알림인지 검증 후 status=READ


2-11-3. (백엔드 내부) 알림 스케줄러

- NotificationScheduler.processDueNotifications()
  - status = PENDING + scheduledAt <= now 인 것들을 SENT로 변경
  - 실제 푸시/메일 연동은 향후 추가 가능


------------------------------------------------------------
2-12. Admin Dashboard / Init
------------------------------------------------------------

2-12-1. 대시보드 합계

- Method: GET
- Path  : /api/admin/dashboard
- Auth  : ROLE_ADMIN
- Response: ApiResponse<DashboardSummary>

DashboardSummary:

{
  "totalUsers": 120,
  "totalPosts": 45,
  "totalWorkspaces": 20,
  "totalSessions": 180,
  "totalAssignments": 60,
  "totalSubmissions": 300,
  "totalFeedbacks": 150
}


2-12-2. KPI 요약

- Method: GET
- Path  : /api/admin/dashboard/kpi
- Auth  : ROLE_ADMIN
- Response: ApiResponse<KpiSummary>

KpiSummary:

{
  "matchingSuccessRate": 75.3,
  "sessionCompletionRate": 88.0,
  "assignmentSubmissionRate": 92.5,
  "averageAttendanceRate": 90.1
}


2-12-3. 관리자 init ping

- Method: POST
- Path  : /api/admin/init/ping
- Auth  : ROLE_ADMIN
- Response: ApiResponse<String>
  - data: "admin init endpoint is alive"


2-12-4. bootstrap (초기 데이터 세팅용)

- Method: POST
- Path  : /api/admin/init/bootstrap
- Auth  : ROLE_ADMIN
- 현재는 "bootstrap placeholder"만 리턴
- 추후 전공/학기/태그 등 초기 데이터 자동 입력용으로 확장 가능


------------------------------------------------------------
2-13. Activity Log (내부용)
------------------------------------------------------------

- 외부로 노출되는 별도 컨트롤러는 현재 없음
- ActivityLoggingAspect가 모든 @RestController 메서드 호출 후,
  ActivityLogService.logSimple(ActivityType.OTHER, "Class#method", ...) 호출
- DB: activity_log
  - activity_log_id, user_id, activity_type, action, created_at ...

향후 필요 시:
- /api/admin/activity-logs 등으로 관리자용 검색/조회 API 추가 가능


=
3. 운영 파이프라인 ↔ API 매핑 (요약)
============================================================

0) 초기 세팅 (관리자)
- 전공:            GET/POST /api/academic/majors
- 학기:            GET/POST /api/academic/semesters
- 프로그램:        GET/POST /api/academic/programs
- 기본 태그:       POST /api/tags (system=true, matchable=true)

1) 회원가입 & 프로필
- 회원가입:        POST /auth/signup
- 로그인:          POST /auth/login
- 내 프로필:       GET /api/users/me
- 내 태그:         GET/PUT /api/users/me/tags
- 내 가용시간:     GET/PUT /api/users/me/availability

2) 모집글/요청글
- 글 생성:         POST /api/posts
- 글 상세:         GET /api/posts/{postId}
- 글 목록:         GET /api/posts
- 글 수정/삭제:    PUT/DELETE /api/posts/{postId}

3) 매칭 (수동+자동)
- 수동 신청:       POST /api/post-applications
- 보낸 신청:       GET /api/post-applications/me/sent
- 받은 신청:       GET /api/post-applications/me/received
- 수락/거절:       POST /api/post-applications/{id}/accept|reject
- 자동 추천:       GET /api/programs/{programId}/matching/recommendations
- 추천 설정:       GET/PUT /api/programs/{programId}/matching/config

4) 워크스페이스/연락처
- 내 워크스페이스: GET /api/workspaces/me
- 워크스페이스 상세: GET /api/workspaces/{workspaceId}
- 수동 생성(관리자):  POST /api/workspaces

5) 세션 운영
- 세션 생성:       POST /api/workspaces/{workspaceId}/sessions
- 세션 목록/상세:   GET /api/workspaces/{workspaceId}/sessions[/id]
- 세션 수정:       PUT /api/workspaces/{workspaceId}/sessions/{sessionId}
- 출석 조회/기록:   GET/POST /api/sessions/{sessionId}/attendance

6) 과제
- 과제 생성/조회/수정:
  - /api/workspaces/{workspaceId}/assignments
- 제출/피드백:
  - /api/assignments/{assignmentId}/submissions

7) 만족도/피드백
- 작성/조회:
  - /api/workspaces/{workspaceId}/feedbacks

8) 대시보드/KPI
- 합계:            GET /api/admin/dashboard
- KPI:             GET /api/admin/dashboard/kpi


=
4. 프론트엔드 개발자용 한 줄 요약
============================================================

1) 로그인 후:
   - accessToken 저장
   - 모든 /api/** 요청에 Authorization: Bearer <accessToken> 붙이기

2) 초반 세팅 화면:
   - 전공/학기/프로그램/태그 조회 → Academic + Tag API

3) 멘토/멘티 마이페이지:
   - /api/users/me
   - /api/users/me/tags
   - /api/users/me/availability
   - /api/workspaces/me

4) 매칭/게시판:
   - /api/posts (+ 필터)
   - /api/post-applications/**
   - /api/programs/{id}/matching/**

5) 워크스페이스 내부 화면:
   - /api/workspaces/{workspaceId}
   - /api/workspaces/{workspaceId}/sessions
   - /api/workspaces/{workspaceId}/assignments
   - /api/sessions/{sessionId}/attendance
   - /api/assignments/{assignmentId}/submissions
   - /api/workspaces/{workspaceId}/feedbacks

6) 관리자 페이지:
   - /api/academic/**
   - /api/admin/dashboard**
   - /api/admin/init/**

이 문서 하나만 갖고 있어도,
프론트엔드에서 어떤 화면에서 어떤 API를 호출해야 할지
대부분 바로 연결될 수 있도록 구성했다.
(필드 세부 타입은 각 DTO 코드 참고)
