package com.waf.waf.dto;

public class WafResult {

    private boolean blocked;
    private int statusCode;
    private String errorMessage;
    private boolean fromCache; // FIX: track whether result came from verdict cache

    public WafResult() {}

    public WafResult(boolean blocked, int statusCode, String errorMessage, boolean fromCache) {
        this.blocked = blocked;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.fromCache = fromCache;
    }

    public boolean isBlocked() { return blocked; }
    public int getStatusCode() { return statusCode; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isFromCache() { return fromCache; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }

    public static WafResult allow() {
        return new WafResult(false, 200, "Allowed", false);
    }

    public static WafResult block(int statusCode, String message) {
        return new WafResult(true, statusCode, message, false);
    }

    // FIX: fromCache=true so WafFilter skips recording a new violation
    public static WafResult fromCache(WafResult original) {
        return new WafResult(original.blocked, original.statusCode, original.errorMessage, true);
    }

    // Serialize: only persist blocked/statusCode/errorMessage — not fromCache
    @Override
    public String toString() {
        return blocked + ";" + statusCode + ";" + (errorMessage != null ? errorMessage : "");
    }

    // Deserialize: fromCache defaults to false on read (will be set by VerdictCache)
    public static WafResult fromString(String str) {
        String[] parts = str.split(";", 3);
        boolean blocked = Boolean.parseBoolean(parts[0]);
        int code = Integer.parseInt(parts[1]);
        String msg = parts.length > 2 ? parts[2] : null;
        return new WafResult(blocked, code, msg, false);
    }
}