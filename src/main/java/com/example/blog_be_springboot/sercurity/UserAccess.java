package com.example.blog_be_springboot.sercurity;

import com.example.blog_be_springboot.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccess")
public class UserAccess {
    private final UserRepository userRepository;
    public UserAccess(UserRepository userRepository) { this.userRepository = userRepository; }

    public boolean canView(Long id, Authentication auth) {
        if (auth == null) return false;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        String me = auth.getName();
        return userRepository.findById(id)
                .map(u -> u.getUsername().equals(me))
                .orElse(false);
    }
}
