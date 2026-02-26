package com.myks.myksbk.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final Long companyId;
    private final String account;
    private final String name;
    private final String profileName;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
            Long id,
            Long companyId,
            String account,
            String name,
            String profileName,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.companyId = companyId;
        this.account = account;
        this.name = name;
        this.profileName = profileName;
        this.authorities = (authorities == null) ? Collections.emptyList() : authorities;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getProfileName() { return profileName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return ""; } // JWT 인증에서는 비번 사용 안 함

    @Override
    public String getUsername() { return account; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}