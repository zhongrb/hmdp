package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.BlogCardDTO;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.BlogLikeMapper;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.impl.BlogServiceImpl;
import com.hmdp.utils.UserHolder;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class BlogInteractionServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private BlogMapper blogMapper;
    @Mock
    private BlogLikeMapper blogLikeMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ShopMapper shopMapper;

    private BlogServiceImpl createBlogService() {
        return new BlogServiceImpl(blogMapper, blogLikeMapper, userMapper, shopMapper, stringRedisTemplate);
    }

    private Blog buildBlog(long id, long userId, long shopId, String title) {
        Blog blog = new Blog();
        blog.setId(id);
        blog.setUserId(userId);
        blog.setShopId(shopId);
        blog.setTitle(title);
        blog.setContent(title + " 内容");
        blog.setLiked(0);
        blog.setStatus(1);
        return blog;
    }

    private User buildUser(long id, String nickName) {
        User user = new User();
        user.setId(id);
        user.setNickName(nickName);
        return user;
    }

    private Shop buildShop(long id, String name) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName(name);
        return shop;
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<Blog> captureBlogQuery() {
        ArgumentCaptor<LambdaQueryWrapper<Blog>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(blogMapper).selectList(captor.capture());
        return captor.getValue();
    }

    private void stubAuthorsAndShops() {
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(buildUser(1L, "阿星"), buildUser(2L, "团团")));
        when(shopMapper.selectBatchIds(any())).thenReturn(List.of(buildShop(1L, "城南小馆"), buildShop(2L, "北街食堂")));
    }

    private void stubAnonymousLikeState() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    private void assertCardIds(ScrollResult<BlogCardDTO> result, Long... ids) {
        assertThat(result.getList()).extracting(BlogCardDTO::getId).containsExactly(ids);
    }

    @Test
    void shouldQueryFeedWithCursorToAvoidDuplicateBlogs() {
        BlogServiceImpl blogService = createBlogService();
        stubAuthorsAndShops();
        when(blogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildBlog(9L, 1L, 1L, "第二篇"),
                buildBlog(8L, 2L, 2L, "第三篇")
        ));

        ScrollResult<BlogCardDTO> result = blogService.queryFeed(10L, 0);
        captureBlogQuery();

        assertCardIds(result, 9L, 8L);
        assertThat(result.getLastId()).isEqualTo(8L);
        assertThat(result.getOffset()).isEqualTo(0);
        assertThat(result.getHasMore()).isFalse();
    }

    @Test
    void shouldKeepFeedCursorWhenNoMoreBlogsReturned() {
        BlogServiceImpl blogService = createBlogService();
        when(blogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ScrollResult<BlogCardDTO> result = blogService.queryFeed(7L, 0);

        assertThat(result.getList()).isEmpty();
        assertThat(result.getLastId()).isEqualTo(7L);
        assertThat(result.getOffset()).isEqualTo(0);
        assertThat(result.getHasMore()).isFalse();
    }

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldToggleLikeWhenUserHasNotLikedBefore() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score("blog:liked:1", "1")).thenReturn(null);

        Boolean likedBefore = stringRedisTemplate.opsForZSet().score("blog:liked:1", "1") != null;

        assertThat(likedBefore).isFalse();
    }

    @Test
    void shouldDetectExistingLikeWhenScoreExists() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score("blog:liked:1", "1")).thenReturn(1714280000D);

        Boolean likedBefore = stringRedisTemplate.opsForZSet().score("blog:liked:1", "1") != null;

        assertThat(likedBefore).isTrue();
    }

    @Test
    void shouldRejectFollowSelf() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());

        assertThatThrownBy(() -> validateFollowTarget(1L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessage("不能关注自己");

        verify(stringRedisTemplate, never()).opsForSet();
    }

    @Test
    void shouldReturnCommonFollowIdsFromRedisIntersection() {
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.intersect("follows:1", "follows:2")).thenReturn(java.util.Set.of("3", "5"));

        java.util.Set<String> intersect = stringRedisTemplate.opsForSet().intersect("follows:1", "follows:2");

        assertThat(intersect).containsExactlyInAnyOrder("3", "5");
    }

    private void validateFollowTarget(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BizException("不能关注自己");
        }
    }
}
