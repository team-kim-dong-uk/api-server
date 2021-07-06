package com.udhd.apiserver.config.auth;

import com.udhd.apiserver.config.auth.dto.OAuthAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.*;

public class CustomOAuth2User implements OAuth2User, Serializable {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Set<GrantedAuthority> authorities;

    private final Map<String, Object> attributes;

    private final String nameAttributeKey;

    private final String email;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities, OAuthAttributes oAuthAttributes) {
        this.authorities = (authorities != null)
                ? Collections.unmodifiableSet(new LinkedHashSet<>(this.sortAuthorities(authorities)))
                : Collections.unmodifiableSet(new LinkedHashSet<>(AuthorityUtils.NO_AUTHORITIES));
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(oAuthAttributes.getAttributes()));
        this.nameAttributeKey = oAuthAttributes.getNameAttributeKey();
        this.email = oAuthAttributes.getEmail();
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return this.getAttribute(this.nameAttributeKey).toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    private Set<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(
                Comparator.comparing(GrantedAuthority::getAuthority));
        sortedAuthorities.addAll(authorities);
        return sortedAuthorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomOAuth2User that = (CustomOAuth2User) o;
        return Objects.equals(authorities, that.authorities) && Objects.equals(attributes, that.attributes) && Objects.equals(nameAttributeKey, that.nameAttributeKey) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorities, attributes, nameAttributeKey, email);
    }

    @Override
    public String toString() {
        return "CustomOAuth2User{" +
                "authorities=" + authorities +
                ", attributes=" + attributes +
                ", nameAttributeKey='" + nameAttributeKey + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
