package com.mentoring.mentoringbackend.activity.domain;

public enum ActivityType {

    LOGIN,
    LOGOUT,

    VIEW_POST,
    CREATE_POST,
    UPDATE_POST,
    DELETE_POST,
    APPLY_POST,

    VIEW_WORKSPACE,
    CREATE_WORKSPACE,

    VIEW_SESSION,
    CREATE_SESSION,
    UPDATE_SESSION,

    SUBMIT_ASSIGNMENT,

    OTHER // 그 외 일반적인 활동
}
