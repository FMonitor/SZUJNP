package com.lcmonitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CookieManager {
    private static final AtomicLong visitorCounter = new AtomicLong(0);
    private static final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static class SessionInfo {
        private final String sessionId;
        private final long visitorId;
        private final LocalDateTime firstVisit;
        private LocalDateTime lastVisit;
        private int visitCount;
        
        public SessionInfo(String sessionId, long visitorId) {
            this.sessionId = sessionId;
            this.visitorId = visitorId;
            this.firstVisit = LocalDateTime.now();
            this.lastVisit = LocalDateTime.now();
            this.visitCount = 1;
        }
        
        public void updateVisit() {
            this.lastVisit = LocalDateTime.now();
            this.visitCount++;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public long getVisitorId() { return visitorId; }
        public LocalDateTime getFirstVisit() { return firstVisit; }
        public LocalDateTime getLastVisit() { return lastVisit; }
        public int getVisitCount() { return visitCount; }
    }
    
    public static SessionInfo createOrUpdateSession(String existingSessionId) {
        if (existingSessionId != null && sessions.containsKey(existingSessionId)) {
            // 更新现有会话
            SessionInfo session = sessions.get(existingSessionId);
            session.updateVisit();
            return session;
        } else {
            // 创建新会话
            long visitorId = visitorCounter.incrementAndGet();
            String sessionId = "SESSION_" + System.currentTimeMillis() + "_" + visitorId;
            SessionInfo session = new SessionInfo(sessionId, visitorId);
            sessions.put(sessionId, session);
            return session;
        }
    }
    
    public static SessionInfo getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public static int getTotalVisitors() {
        return (int) visitorCounter.get();
    }
    
    public static int getActiveSessions() {
        return sessions.size();
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }
}