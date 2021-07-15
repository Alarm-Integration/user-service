package com.gabia.avengers.userservice.dto.response;

import com.gabia.avengers.userservice.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String username;

    public UserInfoResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}
