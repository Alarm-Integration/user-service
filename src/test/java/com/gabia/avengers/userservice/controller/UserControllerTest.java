package com.gabia.avengers.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabia.avengers.userservice.domain.User;
import com.gabia.avengers.userservice.dto.request.LoginRequest;
import com.gabia.avengers.userservice.dto.request.ModifyRequest;
import com.gabia.avengers.userservice.dto.request.SignupRequest;
import com.gabia.avengers.userservice.repository.UserRepository;
import com.gabia.avengers.userservice.service.CustomUserDetailsService;
import com.gabia.avengers.userservice.util.JwtUtils;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String username = "defaultUser";
    private String password = "123456";
    private Long userId = 1L;
    private User defaultUser;

    private URI uri(String path) throws URISyntaxException {
        return new URI(format("/user-service%s", path));
    }

    @BeforeEach
    void before() {
        userRepository.deleteAll();

        defaultUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(defaultUser);
        userId = defaultUser.getId();
    }

    @Test
    void createUser_성공() throws Exception {
        //given
        SignupRequest signupRequest = SignupRequest.builder()
                .username("newUser")
                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post("/user-service/signup")
                .content(asJsonString(signupRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원 가입 성공"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void createUser_실패_username_없음() throws Exception {
        //given
        SignupRequest signupRequest = SignupRequest.builder()
//                .username(username)
                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signup"))
                .content(asJsonString(signupRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("BindException"))
                .andExpect(jsonPath("$.result.errors[0].field").value("username"))
                .andExpect(jsonPath("$.result.errors[0].code").value("NotBlank"));
    }

    @Test
    void createUser_실패_password_없음() throws Exception {
        //given
        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
//                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signup"))
                .content(asJsonString(signupRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("BindException"))
                .andExpect(jsonPath("$.result.errors[0].field").value("password"))
                .andExpect(jsonPath("$.result.errors[0].code").value("NotBlank"));
    }

    @Test
    void createUser_실패_username_중복() throws Exception {
        //given
        SignupRequest signupRequest = SignupRequest.builder()
                .username(username)
                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signup"))
                .content(asJsonString(signupRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("BindException"))
                .andExpect(jsonPath("$.result.errors[0].field").value("username"))
                .andExpect(jsonPath("$.result.errors[0].code").value("duplicate"));
    }

    @Test
    void authenticateUser_성공() throws Exception {
        //given
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signin"))
                .content(asJsonString(loginRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.username").value(username));
    }

    @Test
    void authenticateUser_실패_존재하지않는_username() throws Exception {
        //given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("123")
                .password(password)
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signin"))
                .content(asJsonString(loginRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void authenticateUser_실패_password_틀림() throws Exception {
        //given
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password("wrongPassword")
                .build();

        //when
        ResultActions result = this.mockMvc.perform(post(uri("/signin"))
                .content(asJsonString(loginRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void findUser_성공() throws Exception {
        //given
        String token = getToken(username, password);

        //when
        ResultActions result = mockMvc.perform(get(uri("/" + userId))
                .header("Authorization", format("Bearer %s", token))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 조회 성공"))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.username").value(username));
    }

    @Test
    void findUser_실패_다른_사용자_정보_조회() throws Exception {
        //given
        String token = getToken(username, password);

        //when
        ResultActions result = this.mockMvc.perform(get(uri("/" + userId + 1))
                .header("Authorization", format("Bearer %s", token))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void findUser_실패_토큰_없음() throws Exception {
        //given

        //when
        ResultActions result = this.mockMvc.perform(get(uri("/" + userId))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void modifyUser_성공() throws Exception {
        //given
        ModifyRequest modifyRequest = ModifyRequest.builder()
                .password("newPassword")
                .build();

        String token = getToken(username, password);

        //when
        ResultActions result = mockMvc.perform(patch(uri("/" + userId))
                .header("Authorization", format("Bearer %s", token))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(modifyRequest))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보 수정 성공"))
                .andExpect(jsonPath("$.result.id").value(userId))
                .andExpect(jsonPath("$.result.username").value(username));
    }

    @Test
    void modifyUser_실패_다른_사용자_수정() throws Exception {
        //given
        ModifyRequest modifyRequest = ModifyRequest.builder()
                .password("newPassword")
                .build();

        String token = getToken(username, password);

        //when
        ResultActions result = mockMvc.perform(patch(uri("/" + userId + 1))
                .header("Authorization", format("Bearer %s", token))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(modifyRequest))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void modifyUser_실패_토큰_없음() throws Exception {
        //given
        ModifyRequest modifyRequest = ModifyRequest.builder()
                .password("newPassword")
                .build();

        //when
        ResultActions result = mockMvc.perform(patch(uri("/" + userId + 1))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(modifyRequest))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void deleteUser_성공() throws Exception {
        //given
        String token = getToken(username, password);

        //when
        ResultActions result = mockMvc.perform(delete(uri("/" + userId))
                .header("Authorization", format("Bearer %s", token))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 삭제 성공"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void deleteUser_실패_다른_사용자_삭제() throws Exception {
        //given
        String token = getToken(username, password);

        //when
        ResultActions result = mockMvc.perform(delete(uri("/" + userId + 1))
                .header("Authorization", format("Bearer %s", token))
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void deleteUser_실패_토큰_없음() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(delete("/" + userId)
                .accept(APPLICATION_JSON));

        //then
        result
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getToken(String username, String password) throws NotFoundException {
        UserDetails userDetails = customUserDetailsService.loadUserByUserId(userId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String jwtToken = jwtUtils.generateJwtToken(authentication);
        return jwtToken;
    }
}