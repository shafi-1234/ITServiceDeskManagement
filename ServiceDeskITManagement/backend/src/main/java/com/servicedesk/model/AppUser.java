package com.servicedesk.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Only set for users who registered with a username/password. */
    @Column(unique = true)
    private String username;

    /** BCrypt hash; null for Google-only users. */
    @Column(name = "password_hash")
    private String password;

    /** true once the email is proven (Google login, or created by the admin bootstrap). */
    private boolean emailVerified;

    private String name;
    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
}
