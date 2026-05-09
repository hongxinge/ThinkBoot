package com.thinkboot.auth.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class LoginUser implements Serializable {

    private Long userId;

    private String username;

    private String nickname;

    private Set<String> permissions;

    private Set<String> roles;
}