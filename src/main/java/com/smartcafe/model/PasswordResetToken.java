package com.smartcafe.model;

import java.time.LocalDateTime;

/** A single-use, time-limited token that authorises a password reset. */
public class PasswordResetToken {

    private int           id;
    private int           userId;
    private String        token;
    private LocalDateTime expiresAt;
    private boolean       used;
    private LocalDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(int userId, String token, LocalDateTime expiresAt) {
        this.userId    = userId;
        this.token     = token;
        this.expiresAt = expiresAt;
        this.used      = false;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }

    public String getToken()                    { return token; }
    public void setToken(String token)          { this.token = token; }

    public LocalDateTime getExpiresAt()                 { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt)   { this.expiresAt = expiresAt; }

    public boolean isUsed()                     { return used; }
    public void setUsed(boolean used)           { this.used = used; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
}
