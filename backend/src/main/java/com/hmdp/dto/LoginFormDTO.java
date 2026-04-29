package com.hmdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginFormDTO {

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$")
    private String phone;

    @Pattern(regexp = "^\\d{6}$")
    private String code;
}
