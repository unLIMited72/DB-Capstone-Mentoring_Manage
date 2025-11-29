package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactInfoRequest {

    @NotNull
    private ContactType type;

    @NotBlank
    private String value;

    private boolean primary;

    private boolean visibleToWorkspaceMembers;
}
