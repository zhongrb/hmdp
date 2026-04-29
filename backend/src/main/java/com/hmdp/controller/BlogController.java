package com.hmdp.controller;

import com.hmdp.dto.BlogCardDTO;
import com.hmdp.dto.BlogPublishDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping
    // 发布属于登录后动作，是否已登录由拦截器统一处理，控制器只负责参数校验与调用服务。
    public Result<BlogCardDTO> publish(@Valid @RequestBody BlogPublishDTO payload) {
        return Result.ok(blogService.publish(payload));
    }

    @GetMapping("/feed")
    // 内容流允许匿名浏览，登录态只会影响点赞态等附加信息，不影响公开阅读主路径。
    public Result<ScrollResult<BlogCardDTO>> queryFeed(@RequestParam(required = false) Long lastId,
                                                       @RequestParam(required = false) Integer offset) {
        return Result.ok(blogService.queryFeed(lastId, offset));
    }

    @GetMapping("/hot")
    public Result<java.util.List<BlogCardDTO>> queryHot() {
        return Result.ok(blogService.queryHot());
    }

    @PostMapping("/{blogId}/like")
    public Result<Boolean> toggleLike(@PathVariable Long blogId) {
        return Result.ok(blogService.toggleLike(blogId));
    }
}
