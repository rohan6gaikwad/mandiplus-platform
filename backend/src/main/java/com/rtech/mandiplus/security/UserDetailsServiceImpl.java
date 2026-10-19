package com.rtech.mandiplus.security;

import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + cleanEmail));

        if (user.getActive() != null && !user.getActive()) {
            throw new UsernameNotFoundException("User account is deactivated. Please contact support.");
        }

        return UserDetailsImpl.build(user);
    }
}
