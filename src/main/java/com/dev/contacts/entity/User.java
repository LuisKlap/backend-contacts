package com.dev.contacts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_email", columnNames = { "email" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "full_name", length = 150, nullable = false)
  private String fullName;

  @Column(name = "email", length = 255, nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", length = 255, nullable = false)
  private String passwordHash;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Contact> contacts = new HashSet<>();

  // --- 2FA ---
  @Column(name = "two_factor_enabled", nullable = false)
  @Builder.Default
  private boolean twoFactorEnabled = false;

  @Column(name = "two_factor_secret", length = 64)
  private String twoFactorSecret;

  @Column(name = "two_factor_last_sent")
  private OffsetDateTime twoFactorLastSent;

  @Column(name = "two_factor_temp_code", length = 8)
  private String twoFactorTempCode;

  @Column(name = "two_factor_temp_code_expiry")
  private OffsetDateTime twoFactorTempCodeExpiry;

  @Column(name = "two_factor_type", length = 16)
  @Builder.Default
  private String twoFactorType = "email"; // "email" ou "authenticator"

  public void addContact(Contact contact) {
    contacts.add(contact);
    contact.setOwner(this);
  }

  public void removeContact(Contact contact) {
    contacts.remove(contact);
    contact.setOwner(null);
  }

  @Override
  public Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() {
    return Collections.emptySet();
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
