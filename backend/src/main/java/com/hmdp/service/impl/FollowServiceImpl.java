package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.FollowService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void follow(Long targetUserId) {
        UserDTO currentUser = requireUser();
        validateTarget(currentUser.getId(), targetUserId);
        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new BizException("关注目标不存在");
        }
        Long existed = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, currentUser.getId())
                .eq(Follow::getFollowUserId, targetUserId));
        if (existed != null && existed > 0) {
            throw new BizException("你已经关注过该用户");
        }
        Follow follow = new Follow();
        follow.setUserId(currentUser.getId());
        follow.setFollowUserId(targetUserId);
        followMapper.insert(follow);
        stringRedisTemplate.opsForSet().add(RedisConstants.FOLLOWS_KEY + currentUser.getId(), String.valueOf(targetUserId));
        log.info("关注用户成功，userId={}, targetUserId={}", currentUser.getId(), targetUserId);
    }

    @Override
    @Transactional
    public void unfollow(Long targetUserId) {
        UserDTO currentUser = requireUser();
        followMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, currentUser.getId())
                .eq(Follow::getFollowUserId, targetUserId));
        stringRedisTemplate.opsForSet().remove(RedisConstants.FOLLOWS_KEY + currentUser.getId(), String.valueOf(targetUserId));
        log.info("取消关注成功，userId={}, targetUserId={}", currentUser.getId(), targetUserId);
    }

    @Override
    public List<UserDTO> queryCommonFollows(Long targetUserId) {
        UserDTO currentUser = requireUser();
        // 共同关注直接复用 Redis Set 交集，避免每次都回表做双向关注关系拼装。
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(
                RedisConstants.FOLLOWS_KEY + currentUser.getId(),
                RedisConstants.FOLLOWS_KEY + targetUserId
        );
        if (intersect == null || intersect.isEmpty()) {
            return List.of();
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).toList();
        return userMapper.selectBatchIds(ids).stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .nickName(user.getNickName())
                        .icon(user.getIcon())
                        .build())
                .collect(Collectors.toList());
    }

    private UserDTO requireUser() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BizException("当前操作需要登录后才能进行");
        }
        return user;
    }

    private void validateTarget(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BizException("不能关注自己");
        }
    }
}
