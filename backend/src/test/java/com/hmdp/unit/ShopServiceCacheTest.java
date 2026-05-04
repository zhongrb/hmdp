package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.config.AppProperties;
import com.hmdp.entity.Shop;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ShopServiceCacheTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private GeoOperations<String, String> geoOperations;
    @Mock
    private ShopMapper shopMapper;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Shop buildShop(long id, long typeId, double x, double y, String name) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setTypeId(typeId);
        shop.setName(name);
        shop.setAddress(name + " 路 1 号");
        shop.setX(BigDecimal.valueOf(x));
        shop.setY(BigDecimal.valueOf(y));
        shop.setScore(BigDecimal.valueOf(4.8));
        shop.setComments(100);
        return shop;
    }

    private CacheClient createCacheClient() {
        return new CacheClient(stringRedisTemplate, objectMapper);
    }

    private ShopServiceImpl createShopService() {
        AppProperties appProperties = new AppProperties();
        return new ShopServiceImpl(shopMapper, createCacheClient(), stringRedisTemplate, appProperties);
    }

    @Test
    void shouldReturnCachedShopWhenLogicalExpireNotReached() throws Exception {
        CacheClient cacheClient = createCacheClient();
        Shop cached = buildShop(1L, 2L, 120.1, 30.2, "城南小馆");
        RedisData redisData = new RedisData();
        redisData.setData(cached);
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(20));

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cache:shop:1")).thenReturn(objectMapper.writeValueAsString(redisData));

        Shop result = cacheClient.queryWithLogicalExpire(
                RedisConstants.CACHE_SHOP_KEY,
                RedisConstants.LOCK_SHOP_KEY,
                1L,
                Shop.class,
                Duration.ofMinutes(30),
                () -> null
        );

        assertThat(result.getName()).isEqualTo("城南小馆");
        verify(valueOperations, never()).setIfAbsent(any(), any(), any(Duration.class));
    }

    @Test
    void shouldCreateShopAndMaintainGeoAndCache() {
        ShopServiceImpl shopService = createShopService();
        Shop shop = buildShop(10L, 3L, 121.5, 31.2, "江边茶馆");
        shop.setId(null);

        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(shopMapper.insert(any(Shop.class))).thenAnswer(invocation -> {
            Shop inserted = invocation.getArgument(0);
            inserted.setId(10L);
            return 1;
        });

        Shop result = shopService.createShop(shop);

        assertThat(result.getId()).isEqualTo(10L);
        verify(shopMapper).insert(shop);
        verify(geoOperations).add(
                RedisConstants.SHOP_GEO_KEY + 3L,
                new Point(121.5, 31.2),
                "10"
        );
        verify(stringRedisTemplate).delete(RedisConstants.CACHE_SHOP_KEY + 10L);
    }

    @Test
    void shouldMoveGeoAndDeleteCacheWhenUpdatingTypeOrLocation() {
        ShopServiceImpl shopService = createShopService();
        Shop oldShop = buildShop(5L, 1L, 120.0, 30.0, "旧店");
        Shop updatedShop = buildShop(5L, 2L, 121.0, 31.0, "新店");

        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(shopMapper.selectById(5L)).thenReturn(oldShop, updatedShop);
        when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

        Shop result = shopService.updateShop(5L, updatedShop);

        assertThat(result.getTypeId()).isEqualTo(2L);
        verify(geoOperations).remove(RedisConstants.SHOP_GEO_KEY + 1L, "5");
        verify(geoOperations).add(RedisConstants.SHOP_GEO_KEY + 2L, new Point(121.0, 31.0), "5");
        verify(stringRedisTemplate).delete(RedisConstants.CACHE_SHOP_KEY + 5L);
    }

    @Test
    void shouldDeleteShopGeoAndCacheWhenDeletingShop() {
        ShopServiceImpl shopService = createShopService();
        Shop existing = buildShop(8L, 4L, 120.8, 30.8, "待删店铺");

        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        when(shopMapper.selectById(8L)).thenReturn(existing);
        when(shopMapper.deleteById(8L)).thenReturn(1);

        shopService.deleteShop(8L);

        verify(shopMapper).deleteById(8L);
        verify(geoOperations).remove(RedisConstants.SHOP_GEO_KEY + 4L, "8");
        verify(stringRedisTemplate).delete(RedisConstants.CACHE_SHOP_KEY + 8L);
    }

    @Test
    void shouldThrowWhenShopDoesNotExist() {
        ShopServiceImpl shopService = createShopService();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cache:shop:99")).thenReturn("__null__");

        assertThatThrownBy(() -> shopService.queryById(99L))
                .isInstanceOf(BizException.class)
                .hasMessage("商户不存在");
    }
}
