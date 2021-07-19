package com.gabia.avengers.userservice.service;

import com.gabia.avengers.userservice.domain.User;
import com.gabia.avengers.userservice.dto.request.ModifyRequest;
import com.gabia.avengers.userservice.dto.request.SignUpRequest;
import com.gabia.avengers.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public User createUser(SignUpRequest request) throws Exception {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new Exception("이미 존재하는 username입니다");
        }

        User user = new User(request.getUsername(),
                encoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저는 존재하지 않습니다"));
    }

    public User modifyUser(Long id, ModifyRequest modifyRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저는 존재하지 않습니다"));

        user.changePassword(encoder.encode(modifyRequest.getPassword()));
        return user;
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저는 존재하지 않습니다"));
        userRepository.delete(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
