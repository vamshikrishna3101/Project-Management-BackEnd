package com.pms.projectmanagement.exception;

public class UnauthorizedAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L; // ✅ fixes serialVersionUID warning too
    public UnauthorizedAccessException(String message) { super(message); }
}