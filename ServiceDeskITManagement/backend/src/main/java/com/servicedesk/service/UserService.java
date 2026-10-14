package com.servicedesk.service;

import com.servicedesk.model.AppUser;
import com.servicedesk.model.Role;
import com.servicedesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final Set<String> adminEmails;

    public UserService(UserRepository users, PasswordEncoder encoder, @Value("${app.admin-emails:}") String admins) {
        this.users = users;
        this.encoder = encoder;
        this.adminEmails = Arrays.stream(admins.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /** Google login: creates the user on first login and refreshes the profile. Email is proven by Google. */
    @Transactional
    public AppUser upsert(String rawEmail, String name, String picture) {
        String email = rawEmail.trim().toLowerCase();
        Optional<AppUser> existing = users.findByEmail(email);
        AppUser u = existing.orElseGet(AppUser::new);
        boolean isNew = existing.isEmpty();

        // Someone may have self-registered with this email before the real owner showed up.
        // Google has now proven ownership, so drop the unverified password.
        if (!u.isEmailVerified() && u.getPassword() != null) {
            u.setPassword(null);
            u.setUsername(null);
        }
        u.setEmail(email);
        u.setName(name);
        u.setPictureUrl(picture);
        u.setEmailVerified(true);
        if (adminEmails.contains(email)) u.setRole(Role.ADMIN);
        else if (isNew) u.setRole(Role.USER);
        return users.save(u);
    }

    /** Username/password sign-up. Self-registered users are always plain USERs. */
    @Transactional
    public AppUser registerLocal(String username, String email, String name, String rawPassword) {
        String un = username.trim().toLowerCase();
        String em = email.trim().toLowerCase();
        if (users.findByUsername(un).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        if (users.findByEmail(em).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already registered. Sign in with Google or your existing account.");
        AppUser u = new AppUser();
        u.setUsername(un);
        u.setEmail(em);
        u.setName(name.trim());
        u.setPassword(encoder.encode(rawPassword));
        u.setEmailVerified(false);
        u.setRole(Role.USER);
        return users.save(u);
    }

    /** Creates the first admin account from configuration if it does not exist yet. */
    @Transactional
    public void bootstrapAdmin(String username, String email, String rawPassword) {
        String un = username.trim().toLowerCase();
        if (users.findByUsername(un).isPresent()) return;
        AppUser u = new AppUser();
        u.setUsername(un);
        u.setEmail(email.trim().toLowerCase());
        u.setName("Administrator");
        u.setPassword(encoder.encode(rawPassword));
        u.setEmailVerified(true);
        u.setRole(Role.ADMIN);
        users.save(u);
    }
}
