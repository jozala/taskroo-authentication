package com.taskroo.authn.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecurityToken implements Serializable {

    private static final long serialVersionUID = -7483170872697362182L;

    private final String id;
    private final String userId;
    private final Set<Integer> userRoles;
    private final Date createTime;
    private final Date lastAccessedTime;
    private final String rememberMeToken;

    public SecurityToken(String id, String userId, Set<Integer> userRoles, Date createTime, Date lastAccessedTime, String rememberMeToken) {
        this.id = id;
        this.userId = userId;
        this.userRoles = userRoles;
        this.createTime = createTime;
        this.lastAccessedTime = lastAccessedTime;
        this.rememberMeToken = rememberMeToken;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getLastAccessedTime() {
        return lastAccessedTime;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }

    @JsonIgnore
    public Set<Integer> getUserRoles() {
        return userRoles;
    }

    public static SecurityToken create(User user) {
        String securityId = UUID.randomUUID().toString();
        Date now = new Date();
        Set<Integer> userRoles = convertRolesToIntegers(user.getRoles());
        return new SecurityToken(securityId, user.getUsername(), userRoles, now, now, null);
    }

    public static SecurityToken createWithRememberMeToken(User user) {
        String securityId = UUID.randomUUID().toString();
        Date now = new Date();
        Set<Integer> userRoles = convertRolesToIntegers(user.getRoles());
        RememberMeToken rememberMeToken = RememberMeToken.createNew(user.getUsername());
        return new SecurityToken(securityId, user.getUsername(), userRoles, now, now, rememberMeToken.toString());
    }

    private static Set<Integer> convertRolesToIntegers(Set<Role> roles) {
        Set<Integer> rolesInInt = new HashSet<>(roles.size());
        for (Role role : roles) {
            rolesInInt.add(role.intValue());
        }
        return rolesInInt;
    }
}