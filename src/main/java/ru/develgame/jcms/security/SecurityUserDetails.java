/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.develgame.jcms.entities.SecurityUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityUserDetails implements UserDetails {
    private final SecurityUser securityUser;

    public SecurityUserDetails(SecurityUser securityUser) {
        this.securityUser = securityUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auth = new ArrayList<>();
        if (securityUser != null)
            auth.add(new SimpleGrantedAuthority("ROLE_USER"));
        return auth;
    }

    @Override
    public String getPassword() {
        if (securityUser != null)
            return securityUser.getPass();

        return "";
    }

    @Override
    public String getUsername() {
        if (securityUser != null)
            return securityUser.getName();

        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        if (securityUser != null)
            return true;

        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (securityUser != null)
            return true;

        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (securityUser != null)
            return true;

        return false;
    }

    @Override
    public boolean isEnabled() {
        if (securityUser != null)
            return true;

        return false;
    }
}
