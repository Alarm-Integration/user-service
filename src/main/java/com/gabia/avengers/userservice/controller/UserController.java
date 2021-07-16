package com.gabia.avengers.userservice.controller;

import com.gabia.avengers.userservice.domain.User;
import com.gabia.avengers.userservice.domain.UserDetailsImpl;
import com.gabia.avengers.userservice.dto.request.LoginRequest;
import com.gabia.avengers.userservice.dto.request.ModifyRequest;
import com.gabia.avengers.userservice.dto.request.SignupRequest;
import com.gabia.avengers.userservice.dto.response.APIResponse;
import com.gabia.avengers.userservice.dto.response.UserInfoResponse;
import com.gabia.avengers.userservice.security.CurrentUser;
import com.gabia.avengers.userservice.service.AuthService;
import com.gabia.avengers.userservice.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "UserController")
@RequiredArgsConstructor
@RequestMapping("/user-service")
@RestController
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @ApiOperation(value = "회원 가입", notes = "회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signUpRequest,
                                        BindingResult bindingResult) throws Exception {
        validateDuplicateUser(signUpRequest.getUsername(), bindingResult);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        userService.createUser(signUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.withMessageAndResult("회원 가입 성공", null));
    }

    private void validateDuplicateUser(String username, BindingResult bindingResult) {
        if (userService.existsByUsername(username)){
            bindingResult.rejectValue("username", "duplicate", new Object[]{username}, null);
        }
    }

    @ApiOperation(value = "로그인", notes = "로그인 성공 시 jwt 토큰을 헤더로 반환합니다")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String jwt = authService.authenticate(loginRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Authorization", jwt)
                .body(APIResponse.withMessageAndResult("로그인 성공", null));
    }

    @ApiOperation(value = "사용자 조회", notes = "사용자를 조회합니다")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> findUser(@CurrentUser UserDetailsImpl currentUser,
                                      @PathVariable Long id) {
        if (!currentUser.getId().equals(id))
            throw new AccessDeniedException("접근 권한이 없습니다");

        User user = userService.findUserById(id);
        UserInfoResponse userInfoResponse = new UserInfoResponse(user);
        return ResponseEntity.ok(APIResponse.withMessageAndResult("회원 조회 성공", userInfoResponse));
    }

    @ApiOperation(value = "사용자 정보 수정", notes = "사용자 정보를 수정합니다")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> modifyUser(@CurrentUser UserDetailsImpl currentUser,
                                        @PathVariable Long id,
                                        @Valid @RequestBody ModifyRequest modifyRequest,
                                        BindingResult bindingResult) throws BindException {
        if (!currentUser.getId().equals(id))
            throw new AccessDeniedException("접근 권한이 없습니다");

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        User user = userService.modifyUser(id, modifyRequest);
        UserInfoResponse userInfoResponse = new UserInfoResponse(user);
        return ResponseEntity.ok(APIResponse.withMessageAndResult("회원 정보 수정 성공", userInfoResponse));
    }

    @ApiOperation(value = "사용자 삭제", notes = "사용자를 삭제합니다")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@CurrentUser UserDetailsImpl currentUser,
                                        @PathVariable Long id) {
        if (!currentUser.getId().equals(id))
            throw new AccessDeniedException("접근 권한이 없습니다");

        userService.deleteUser(id);
        return ResponseEntity.ok(APIResponse.withMessageAndResult("회원 삭제 성공", null));
    }

}
