package com.am.framework.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {

    private Long id;
    private String username;
    private String role;
}
