package com.hmdp.service;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.LoginResultDTO;

public interface UserService {

    void sendCode(String phone);

    LoginResultDTO login(LoginFormDTO loginForm);
}
