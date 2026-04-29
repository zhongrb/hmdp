package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.BlogCardDTO;
import com.hmdp.dto.BlogPublishDTO;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.BlogLike;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.BlogLikeMapper;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.BlogService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private static final int FEED_PAGE_SIZE = 10;
    private static final int HOT_PAGE_SIZE = 10;

    private final BlogMapper blogMapper;
    private final BlogLikeMapper blogLikeMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public BlogCardDTO publish(BlogPublishDTO payload) {
        UserDTO user = requireUser();
        Shop shop = shopMapper.selectById(payload.getShopId());
        if (shop == null) {
            throw new BizException("关联商户不存在");
        }

        Blog blog = new Blog();
        blog.setUserId(user.getId());
        blog.setShopId(payload.getShopId());
        blog.setTitle(payload.getTitle());
        blog.setContent(payload.getContent());
        blog.setImages(payload.getImages());
        blog.setLiked(0);
        blog.setStatus(1);
        blogMapper.insert(blog);
        log.info("探店笔记发布成功，blogId={}, userId={}, shopId={}", blog.getId(), user.getId(), payload.getShopId());
        return toCard(blog, Map.of(user.getId(), userMapper.selectById(user.getId())), Map.of(shop.getId(), shop), Set.of());
    }

    @Override
    public ScrollResult<BlogCardDTO> queryFeed(Long lastId, Integer offset) {
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, 1);
        // 当前内容流按 id 倒序滚动，继续翻页时只取 lastId 之前的数据，避免“继续浏览”重复返回上一页内容。
        if (lastId != null) {
            queryWrapper.lt(Blog::getId, lastId);
        }
        List<Blog> records = blogMapper.selectList(queryWrapper
                .orderByDesc(Blog::getId)
                .last("limit " + FEED_PAGE_SIZE));
        List<BlogCardDTO> cards = enrichCards(records);
        Long nextLastId = cards.isEmpty() ? lastId : cards.get(cards.size() - 1).getId();
        return ScrollResult.<BlogCardDTO>builder()
                .list(cards)
                .lastId(nextLastId)
                .offset(0)
                .hasMore(cards.size() >= FEED_PAGE_SIZE)
                .build();
    }

    @Override
    public List<BlogCardDTO> queryHot() {
        Page<Blog> page = blogMapper.selectPage(Page.of(1, HOT_PAGE_SIZE), new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, 1)
                .orderByDesc(Blog::getLiked)
                .orderByDesc(Blog::getId));
        return enrichCards(page.getRecords());
    }

    @Override
    @Transactional
    public boolean toggleLike(Long blogId) {
        UserDTO user = requireUser();
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getStatus() == null || blog.getStatus() != 1) {
            throw new BizException("探店笔记不存在");
        }
        String key = RedisConstants.BLOG_LIKED_KEY + blogId;
        Double score = stringRedisTemplate.opsForZSet().score(key, String.valueOf(user.getId()));
        if (score == null) {
            BlogLike like = new BlogLike();
            like.setBlogId(blogId);
            like.setUserId(user.getId());
            like.setCreateTime(LocalDateTime.now());
            blogLikeMapper.insert(like);
            blog.setLiked(blog.getLiked() == null ? 1 : blog.getLiked() + 1);
            blogMapper.updateById(blog);
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(user.getId()), System.currentTimeMillis());
            log.info("探店笔记点赞成功，blogId={}, userId={}", blogId, user.getId());
            return true;
        }

        blogLikeMapper.delete(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, user.getId()));
        blog.setLiked(Math.max((blog.getLiked() == null ? 0 : blog.getLiked()) - 1, 0));
        blogMapper.updateById(blog);
        stringRedisTemplate.opsForZSet().remove(key, String.valueOf(user.getId()));
        log.info("探店笔记取消点赞成功，blogId={}, userId={}", blogId, user.getId());
        return false;
    }

    private List<BlogCardDTO> enrichCards(List<Blog> blogs) {
        if (blogs.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = blogs.stream().map(Blog::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> shopIds = blogs.stream().map(Blog::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Shop> shops = shopMapper.selectBatchIds(shopIds).stream().collect(Collectors.toMap(Shop::getId, Function.identity()));
        Set<String> likedIds = queryLikedBlogIds(blogs);
        List<BlogCardDTO> cards = new ArrayList<>();
        for (Blog blog : blogs) {
            cards.add(toCard(blog, users, shops, likedIds));
        }
        cards.sort(Comparator.comparing(BlogCardDTO::getId).reversed());
        return cards;
    }

    private Set<String> queryLikedBlogIds(List<Blog> blogs) {
        UserDTO currentUser = UserHolder.getUser();
        // 匿名用户允许公开浏览内容流，但不需要计算点赞态，直接返回空集合即可。
        if (currentUser == null) {
            return Set.of();
        }
        return blogs.stream()
                .filter(blog -> stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + blog.getId(), String.valueOf(currentUser.getId())) != null)
                .map(blog -> String.valueOf(blog.getId()))
                .collect(Collectors.toSet());
    }

    private BlogCardDTO toCard(Blog blog, Map<Long, User> users, Map<Long, Shop> shops, Set<String> likedIds) {
        User author = users.get(blog.getUserId());
        Shop shop = shops.get(blog.getShopId());
        return BlogCardDTO.builder()
                .id(blog.getId())
                .userId(blog.getUserId())
                .shopId(blog.getShopId())
                .title(blog.getTitle())
                .content(blog.getContent())
                .images(blog.getImages())
                .liked(blog.getLiked())
                .isLiked(likedIds.contains(String.valueOf(blog.getId())))
                .authorName(author == null ? "匿名用户" : author.getNickName())
                .authorIcon(author == null ? null : author.getIcon())
                .shopName(shop == null ? "未知商户" : shop.getName())
                .createTime(blog.getCreateTime())
                .build();
    }

    private UserDTO requireUser() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new BizException("当前操作需要登录后才能进行");
        }
        return user;
    }
}
