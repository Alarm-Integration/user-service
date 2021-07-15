package com.gabia.avengers.userservice.repository;

import com.gabia.avengers.userservice.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void test_findByUsername(){
        //given
        String username = "nys";

        User user = User.builder()
                .username(username)
                .password("123456")
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        //when
        User findUser = userRepository.findByUsername(username).get();

        //then
        assertThat(findUser).isEqualTo(user);
    }

    @Test
    void test_existsByUsername(){
        //given
        String username = "nys";

        User user = User.builder()
                .username(username)
                .password("123456")
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        //when
        Boolean exists = userRepository.existsByUsername(username);

        //then
        assertThat(exists).isTrue();
    }

}