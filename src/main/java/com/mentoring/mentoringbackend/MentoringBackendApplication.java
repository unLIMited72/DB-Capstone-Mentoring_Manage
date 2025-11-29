package com.mentoring.mentoringbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;  // ✅ 추가

@SpringBootApplication
@EnableScheduling  // ✅ 스케줄러 활성화
public class MentoringBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentoringBackendApplication.class, args);
    }
}
