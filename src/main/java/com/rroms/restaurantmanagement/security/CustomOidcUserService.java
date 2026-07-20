package com.rroms.restaurantmanagement.security;

import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.Role;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import com.rroms.restaurantmanagement.entity.constant.UserStatus;
import com.rroms.restaurantmanagement.repository.UserRepository;
import com.rroms.restaurantmanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService {

    private static final int FIRST_NAME_MAX_LENGTH = 20;
    private static final int LAST_NAME_MAX_LENGTH = 20;

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final OidcUserService delegate = new OidcUserService();

    public OidcUser loadUser(OidcUserRequest userRequest) {
        return mapToLocalUser(delegate.loadUser(userRequest));
    }

    OidcUser mapToLocalUser(OidcUser googleUser) {
        String email = googleUser.getEmail();

        if (email == null || email.isBlank() || !isEmailVerified(googleUser)) {
            throw oauthError("unverified_email", "Google account must provide a verified email address.");
        }

        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        User localUser = userRepository.findByUsernameIgnoreCase(normalizedEmail)
                .orElseGet(() -> registerCustomer(googleUser, normalizedEmail));

        if (localUser.getStatus() != UserStatus.ACTIVE) {
            throw oauthError("local_user_disabled", "The linked system account is not active.");
        }
        if (localUser.getRole() == null || localUser.getRole().getRoleName() == null) {
            throw oauthError("local_role_missing", "The linked system account does not have a role.");
        }

        SimpleGrantedAuthority localRole = new SimpleGrantedAuthority(
                "ROLE_" + localUser.getRole().getRoleName().name()
        );

        // Google supplies identity claims; authorization always comes from the local database.
        if (googleUser.getUserInfo() != null) {
            return new CustomOidcUser(
                    List.of(localRole),
                    googleUser.getIdToken(),
                    googleUser.getUserInfo(),
                    "email",
                    localUser
            );
        }
        return new CustomOidcUser(
                List.of(localRole),
                googleUser.getIdToken(),
                "email",
                localUser
        );
    }

    private User registerCustomer(OidcUser googleUser, String email) {
        Role customerRole = roleService.findByRoleName(RoleName.CUSTOMER);
        if (customerRole == null) {
            throw oauthError("customer_role_missing", "The CUSTOMER role is not configured.");
        }

        User customer = User.builder()
                .username(email)
                .passwordHash(null)
                .firstName(truncate(claimAsString(googleUser, "family_name"), FIRST_NAME_MAX_LENGTH))
                .lastName(resolveLastName(googleUser, email))
                .phone(claimAsString(googleUser, "phone_number"))
                .status(UserStatus.ACTIVE)
                .role(customerRole)
                .build();

        try {
            return userRepository.save(customer);
        } catch (DataIntegrityViolationException duplicateEmail) {
            // Another concurrent OAuth callback may have created the same verified email first.
            return userRepository.findByUsernameIgnoreCase(email)
                    .orElseThrow(() -> oauthError(
                            "oauth_registration_failed",
                            "Could not create the local Google account."
                    ));
        }
    }

    private String resolveLastName(OidcUser googleUser, String email) {
        String givenName = claimAsString(googleUser, "given_name");
        if (givenName == null) {
            givenName = claimAsString(googleUser, "name");
        }
        if (givenName == null) {
            givenName = email.substring(0, email.indexOf('@'));
        }
        return truncate(givenName, LAST_NAME_MAX_LENGTH);
    }

    private String claimAsString(OidcUser googleUser, String claimName) {
        Object value = googleUser.getClaims().get(claimName);
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return value.toString().trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isEmailVerified(OidcUser googleUser) {
        Object emailVerified = googleUser.getClaims().get("email_verified");
        return Boolean.TRUE.equals(emailVerified)
                || "true".equalsIgnoreCase(String.valueOf(emailVerified));
    }

    private OAuth2AuthenticationException oauthError(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), description);
    }
}
