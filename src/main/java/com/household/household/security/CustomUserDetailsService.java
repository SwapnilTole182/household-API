package com.household.household.security;

import com.household.household.entity.Admin;
import com.household.household.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try to find by email first
        Optional<Admin> adminOpt = adminRepository.findByEmail(identifier);
        
        // If not found, try to find by mobile
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepository.findByMobile(identifier);
        }

        Admin admin = adminOpt.orElseThrow(() -> 
                new UsernameNotFoundException("User not found with email or mobile: " + identifier));

        return User.builder()
                .username(admin.getEmail())
                .password(admin.getPassword())
                .roles(admin.getRole().name())
                .build();
    }
}
