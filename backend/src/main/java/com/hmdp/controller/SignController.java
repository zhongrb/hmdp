package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping
    // 签到接口必须在登录态下调用，是否已登录由拦截器统一校验，这里只负责业务动作本身。
    public Result<Void> sign() {
        signService.sign();
        return Result.ok();
    }

    @GetMapping("/streak")
    public Result<Integer> queryCurrentStreak() {
        return Result.ok(signService.countCurrentStreak());
    }
}
