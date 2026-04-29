package com.hmdp.service;

import com.hmdp.dto.UserDTO;
import java.util.List;

public interface FollowService {

    void follow(Long targetUserId);

    void unfollow(Long targetUserId);

    List<UserDTO> queryCommonFollows(Long targetUserId);
}
