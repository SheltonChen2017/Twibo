package edu.miis.security;

import edu.miis.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TwiboUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public TwiboUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        edu.miis.domain.User account = users.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles("USER")
                .build();
    }
}
