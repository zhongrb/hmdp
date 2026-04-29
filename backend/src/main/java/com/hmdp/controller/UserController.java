package com.hmdp.controller;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.LoginResultDTO;
import com.hmdp.dto.Result;
import com.hmdp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/code")
    // 发码接口属于公开入口，但仍需先做手机号格式校验，避免无意义请求持续进入短信链路。
    public Result<Void> sendCode(@RequestBody @Validated PhoneLoginRequest request) {
        userService.sendCode(request.getPhone());
        return Result.ok();
    }

    @PostMapping("/login")
    // 登录接口保持公开访问，真正的安全边界依赖验证码校验、一次性消费和后续 token 会话续期。
    public Result<LoginResultDTO> login(@RequestBody @Valid LoginFormDTO loginForm) {
        return Result.ok(userService.login(loginForm));
    }

    public static class PhoneLoginRequest {

        @Pattern(regexp = "^1\\d{10}$")
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
