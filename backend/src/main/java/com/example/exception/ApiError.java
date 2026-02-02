package com.example.exception;

import java.time.LocalDateTime;

public class ApiError {
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String message;

    public ApiError() {}
    public ApiError(int status, String message) { this.status = status; this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}