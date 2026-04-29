package com.hmdp.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BlogCardDTO {
    Long id;
    Long userId;
    Long shopId;
    String title;
    String content;
    String images;
    Integer liked;
    Boolean isLiked;
    String authorName;
    String authorIcon;
    String shopName;
    LocalDateTime createTime;
}
