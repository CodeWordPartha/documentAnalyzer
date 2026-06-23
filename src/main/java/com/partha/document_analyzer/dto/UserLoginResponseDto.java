package com.partha.document_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class UserLoginResponseDto {

    private String token;
    private String type;
    private Long expiresIn;
    private UserResponseDto user;

}
