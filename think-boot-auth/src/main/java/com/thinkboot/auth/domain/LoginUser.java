package com.thinkboot.auth.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String nickname;

    private Set<String> permissions = new HashSet<>();

    private Set<String> roles = new HashSet<>();

    public Set<String> getPermissions() {
        return permissions != null ? Collections.unmodifiableSet(permissions) : Collections.emptySet();
    }

    public Set<String> getRoles() {
        return roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
    }
}