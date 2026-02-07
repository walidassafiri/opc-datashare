package com.example.file;

import java.util.List;

public class UploadRequest {
    private Integer expirationDays;
    private String password;
    private List<String> tags;

    public Integer getExpirationDays() { return expirationDays; }
    public void setExpirationDays(Integer expirationDays) { this.expirationDays = expirationDays; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
