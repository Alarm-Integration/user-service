package com.gabia.avengers.userservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class APIResponse {
    private String message;
    private Object result;

    public static APIResponse withMessageAndResult(String message, Object result) {
        return APIResponse.builder()
                .message(message)
                .result(result)
                .build();
    }
}
