package com.gabia.avengers.userservice.service;

import com.gabia.avengers.userservice.domain.User;
import com.gabia.avengers.userservice.dto.request.ModifyRequest;
import com.gabia.avengers.userservice.dto.request.SignUpRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    private String username = "user1";
    private String password = "123456";

    @Test
    void createUser_성공() throws Exception {
        //given
        SignUpRequest signupRequest = SignUpRequest.builder()
                .username(username)
                .password(password)
                .build();

        //when
        User user = userService.createUser(signupRequest);

        //then
        em.clear();
        User findUser = em.find(User.class, user.getId());
        Assertions.assertThat(user).isEqualTo(findUser);
    }

    @Test
    void createUser_실패_존재하는_username() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        SignUpRequest signupRequest = SignUpRequest.builder()
                .username(username)
                .password(password)
                .build();

        //when
        Throwable throwable = catchThrowable(() -> userService.createUser(signupRequest));

        //then
        assertThat(throwable).isInstanceOf(Exception.class).hasMessageContaining("이미 존재하는 username입니다");
    }

    @Test
    void findUserById_성공() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        //when
        User findUser = userService.findUserById(user.getId());

        //then
        Assertions.assertThat(findUser).isEqualTo(user);
    }

    @Test
    void findUserById_실패_존재하지_않는_ID() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        //when
        Throwable throwable = catchThrowable(() -> userService.findUserById(-1L));

        //then
        assertThat(throwable).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("해당 유저는 존재하지 않습니다");
    }

    @Test
    void modifyUser_성공() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        String modifiedPassword = "654321";
        ModifyRequest modifyRequest = ModifyRequest.builder()
                .password(modifiedPassword)
                .build();

        //when
        User modifiedUser = userService.modifyUser(user.getId(), modifyRequest);

        //then
        Assertions.assertThat(passwordEncoder.matches(modifiedPassword, modifiedUser.getPassword())).isTrue();
    }

    @Test
    void modifyUser_실패_존재하지_않는_ID() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        String modifiedPassword = "654321";
        ModifyRequest modifyRequest = ModifyRequest.builder()
                .password(modifiedPassword)
                .build();

        //when
        Throwable throwable = catchThrowable(() -> userService.modifyUser(-1L, modifyRequest));

        //then
        assertThat(throwable).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("해당 유저는 존재하지 않습니다");
    }

    @Test
    void delete_성공() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        Long userId = user.getId();

        //when
        userService.deleteUser(user.getId());

        //then
        User findUser = em.find(User.class, userId);
        assertThat(findUser).isNull();
    }

    @Test
    void delete_실패_존재하지_않는_ID() {
        //given
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        //when
        Throwable throwable = catchThrowable(() -> userService.deleteUser(-1L));

        //then
        assertThat(throwable).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("해당 유저는 존재하지 않습니다");
    }


}