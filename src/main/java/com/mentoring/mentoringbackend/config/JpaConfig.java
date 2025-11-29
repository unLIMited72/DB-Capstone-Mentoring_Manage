package com.mentoring.mentoringbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing(@CreatedDate, @LastModifiedDate 등)을 위한 설정.
    // 나중에 엔티티에 Auditing 필드를 추가해도 바로 동작합니다.
}
