package com.rroms.restaurantmanagement.security;

import com.rroms.restaurantmanagement.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

public class CustomOidcUser extends DefaultOidcUser {

    private final User user;

    public CustomOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            OidcUserInfo userInfo,
            String nameAttributeKey,
            User user
    ) {
        super(authorities, idToken, userInfo, nameAttributeKey);
        this.user = user;
    }

    public CustomOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            String nameAttributeKey,
            User user
    ) {
        super(authorities, idToken, nameAttributeKey);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
