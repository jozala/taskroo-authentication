package com.taskroo.authn.domain;

import java.io.Serializable;
import java.util.Date;

public class Session implements Serializable {

    private static final long serialVersionUID = -7483170872697362182L;

    private String sessionId;
    private String userId;
    private Date createTime;
    private Date lastAccessedTime;
    private String rememberMeToken;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }

    public void setRememberMeToken(String rememberMeTokenKey) {
        this.rememberMeToken = rememberMeTokenKey;
    }
}
