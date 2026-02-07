package com.example.security;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {
    // token -> expiryMillis
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expiry) {
        if (token == null || expiry == null) return;
        blacklist.put(token, expiry.getTime());
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        // cleanup expired entries lazily
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = blacklist.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (e.getValue() < now) {
                it.remove();
            }
        }
        return blacklist.containsKey(token);
    }
}
