package com.gabia.avengers.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
