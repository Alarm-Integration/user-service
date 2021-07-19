package com.gabia.avengers.userservice.service;

import com.gabia.avengers.userservice.domain.User;
import com.gabia.avengers.userservice.domain.UserDetailsImpl;
import com.gabia.avengers.userservice.repository.UserRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저는 존재하지 않습니다"));
        return UserDetailsImpl.createFrom(user);
    }

    public UserDetails loadUserByUserId(Long userId) throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저는 존재하지 않습니다"));
        return UserDetailsImpl.createFrom(user);
    }
}