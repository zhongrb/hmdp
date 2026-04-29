package com.hmdp.service;

import com.hmdp.dto.BlogCardDTO;
import com.hmdp.dto.BlogPublishDTO;
import com.hmdp.dto.ScrollResult;
import java.util.List;

public interface BlogService {

    BlogCardDTO publish(BlogPublishDTO payload);

    ScrollResult<BlogCardDTO> queryFeed(Long lastId, Integer offset);

    List<BlogCardDTO> queryHot();

    boolean toggleLike(Long blogId);
}
