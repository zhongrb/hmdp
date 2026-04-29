package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdp.config.AppProperties;
import com.hmdp.config.AuthProperties;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.exception.BizException;
import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.impl.UserServiceImpl;
import com.hmdp.utils.RedisConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AuthGuardTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private AppProperties appProperties;
    private AuthProperties authProperties;

    @InjectMocks
    private LoginInterceptor loginInterceptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        authProperties = new AuthProperties();
        userService = new UserServiceImpl(userMapper, stringRedisTemplate, appProperties, authProperties);
    }

    @Test
    void shouldRejectInvalidPhoneWhenSendCode() {
        assertThatThrownBy(() -> userService.sendCode("123"))
                .isInstanceOf(BizException.class)
                .hasMessage("请输入正确的手机号");
    }

    @Test
    void shouldRejectInvalidCodeWhenLogin() {
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setPhone("13800138000");
        loginForm.setCode("000000");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisConstants.LOGIN_CODE_KEY + "13800138000")).thenReturn("123456");

        assertThatThrownBy(() -> userService.login(loginForm))
                .isInstanceOf(BizException.class)
                .hasMessage("验证码无效或已过期");
    }

    @Test
    void shouldWriteCodeToRedisWhenPhoneValid() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        userService.sendCode("13800138000");
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq(RedisConstants.LOGIN_CODE_KEY + "13800138000"), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectProtectedRequestWhenUserNotLoggedIn() {
        assertThatThrownBy(() -> loginInterceptor.preHandle(request, response, new Object()))
                .isInstanceOf(com.hmdp.exception.UnauthorizedException.class)
                .hasMessage("请先完成登录后再继续操作");
    }
}
