package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.config.AppProperties;
import com.hmdp.config.AuthProperties;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.LoginResultDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.UserService;
import com.hmdp.utils.RedisConstants;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;
    private final AuthProperties authProperties;

    @Override
    public void sendCode(String phone) {
        validatePhone(phone);
        String code = generateCode(appProperties.getSms().getCodeLength());
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + phone,
                code,
                Duration.ofMinutes(appProperties.getSms().getCodeTtlMinutes())
        );
        log.info("验证码已生成，phone={}, provider={}, code={}", maskPhone(phone), appProperties.getSms().getProvider(), code);
    }

    @Override
    @Transactional
    public LoginResultDTO login(LoginFormDTO loginForm) {
        validatePhone(loginForm.getPhone());
        if (!StringUtils.hasText(loginForm.getCode())) {
            throw new BizException("请输入验证码");
        }
        String key = RedisConstants.LOGIN_CODE_KEY + loginForm.getPhone();
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(cachedCode) || !cachedCode.equals(loginForm.getCode())) {
            log.info("验证码校验失败，phone={}", maskPhone(loginForm.getPhone()));
            throw new BizException("验证码无效或已过期");
        }
        // 验证码通过后继续走单次消费流程，避免同一条验证码在有效期内被重复登录使用。
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, loginForm.getPhone()));
        if (user == null) {
            user = new User();
            user.setPhone(loginForm.getPhone());
            user.setNickName(buildNickName(loginForm.getPhone()));
            userMapper.insert(user);
            log.info("新用户创建成功，userId={}", user.getId());
        }
        // 验证码一旦使用成功立即删除，保持短信登录的一次性约束。
        stringRedisTemplate.delete(key);

        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstants.LOGIN_TOKEN_KEY + token;
        // 会话信息按 Hash 存入 Redis，供刷新拦截器直接读取并续期。
        Map<String, String> session = new HashMap<>();
        session.put("userId", String.valueOf(user.getId()));
        session.put("nickName", user.getNickName());
        session.put("icon", user.getIcon() == null ? "" : user.getIcon());
        stringRedisTemplate.opsForHash().putAll(tokenKey, session);
        stringRedisTemplate.expire(tokenKey, Duration.ofMinutes(authProperties.getTokenTtlMinutes()));
        log.info("用户登录成功，userId={}", user.getId());

        return LoginResultDTO.builder()
                .token(token)
                .user(UserDTO.builder()
                        .id(user.getId())
                        .nickName(user.getNickName())
                        .icon(user.getIcon())
                        .build())
                .build();
    }

    private void validatePhone(String phone) {
        if (!StringUtils.hasText(phone) || !phone.matches("^1\\d{10}$")) {
            throw new BizException("请输入正确的手机号");
        }
    }

    private String generateCode(int codeLength) {
        int max = (int) Math.pow(10, codeLength);
        int min = (int) Math.pow(10, codeLength - 1);
        return String.valueOf(min + (int) (Math.random() * (max - min)));
    }

    private String buildNickName(String phone) {
        return "用户" + phone.substring(phone.length() - 4);
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
