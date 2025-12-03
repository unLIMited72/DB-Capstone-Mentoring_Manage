package com.mentoring.mentoringbackend.config;

import com.mentoring.mentoringbackend.auth.JwtAuthenticationFilter;
import com.mentoring.mentoringbackend.auth.JwtTokenProvider;
import com.mentoring.mentoringbackend.auth.SecurityUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                  // ✅ WebConfig 의 CORS 설정을 Spring Security도 사용하도록 연결
                .cors(Customizer.withDefaults())
                // CSRF 비활성화 (REST + JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션을 사용하지 않으므로 STATELESS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 0. 인증 불필요 영역
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // 1. "내 정보/내 설정" - 로그인한 사용자 누구나
                        //    ⚠️ 반드시 "/api/users/**"보다 위에 있어야 함!
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()

                        // 2. 그 외 /api/users/** 는 관리자만
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 3. 학사 정보 (전공/학기/프로그램)
                        // 조회는 로그인한 누구나
                        .requestMatchers(HttpMethod.GET, "/api/academic/**").authenticated()
                        // 생성/수정/삭제는 ADMIN만
                        .requestMatchers(HttpMethod.POST, "/api/academic/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/academic/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/academic/**").hasRole("ADMIN")

                        // 4. 태그
                        // 조회(GET): 로그인한 누구나
                        .requestMatchers(HttpMethod.GET, "/api/tags/**").authenticated()

                        // 학생/멘토/멘티도 커스텀 태그 제안 가능
                        .requestMatchers(HttpMethod.POST, "/api/tags/custom").authenticated()

                        // 나머지 태그 생성/수정/삭제: ADMIN만
                        .requestMatchers(HttpMethod.POST, "/api/tags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tags/**").hasRole("ADMIN")

                        // 5. 게시글 / 신청
                        .requestMatchers("/api/posts/**").authenticated()
                        .requestMatchers("/api/post-applications/**").authenticated()

                        // 6. 그 외 나머지 API: 일단 "로그인만 하면" 허용
                        .anyRequest().authenticated()
                )

                // 기본 폼 로그인, httpBasic 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // UserDetailsService 등록
                .userDetailsService(userDetailsService)

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 인증 실패 시 JSON 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            String body = """
                                {"success":false,"data":null,"message":"인증이 필요합니다.","errorCode":"AUTH_UNAUTHORIZED"}
                                """;

                            response.getWriter().write(body);
                        })
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // JwtAuthenticationFilter는 JwtTokenProvider만 받도록 설계
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // DB의 password_hash 가 BCrypt라는 전제
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
