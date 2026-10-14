package com.servicedesk.config;

import com.servicedesk.model.AppUser;
import com.servicedesk.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads username/password accounts. The login field accepts either the username or the email. */
@Service
public class LocalUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public LocalUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String key = login.trim().toLowerCase();
        AppUser u = users.findByUsername(key).or(() -> users.findByEmail(key))
                .filter(x -> x.getPassword() != null)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        // The principal name is the email so the rest of the app treats Google and local users the same way.
        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().name())
                .build();
    }
}
