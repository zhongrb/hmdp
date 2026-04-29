package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.service.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    // 关注/取关/共同关注都属于社交互动能力，需要在登录后才能操作或查看。
    public Result<Void> follow(@PathVariable Long targetUserId) {
        followService.follow(targetUserId);
        return Result.ok();
    }

    @DeleteMapping("/{targetUserId}")
    public Result<Void> unfollow(@PathVariable Long targetUserId) {
        followService.unfollow(targetUserId);
        return Result.ok();
    }

    @GetMapping("/common/{targetUserId}")
    public Result<List<UserDTO>> queryCommon(@PathVariable Long targetUserId) {
        return Result.ok(followService.queryCommonFollows(targetUserId));
    }
}
