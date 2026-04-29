package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.config.AppProperties;
import com.hmdp.entity.Shop;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.ShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private static final int PAGE_SIZE = 10;
    private static final Duration SHOP_CACHE_TTL = Duration.ofMinutes(30);

    private final ShopMapper shopMapper;
    private final CacheClient cacheClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    @Override
    public List<Shop> queryByType(Long typeId, int page) {
        Page<Shop> pageResult = shopMapper.selectPage(
                Page.of(Math.max(page, 1), PAGE_SIZE),
                new LambdaQueryWrapper<Shop>()
                        .eq(typeId != null, Shop::getTypeId, typeId)
                        .orderByDesc(Shop::getScore)
                        .orderByDesc(Shop::getComments)
                        .orderByAsc(Shop::getId)
        );
        return pageResult.getRecords();
    }

    @Override
    public Shop queryById(Long shopId) {
        // 商户详情属于高频公开访问数据，优先走缓存穿透防护链路，避免热点详情持续打到数据库。
        Shop shop = cacheClient.queryWithPassThrough(
                RedisConstants.CACHE_SHOP_KEY,
                shopId,
                Shop.class,
                SHOP_CACHE_TTL,
                () -> shopMapper.selectById(shopId)
        );
        if (shop == null) {
            throw new BizException("商户不存在");
        }
        return shop;
    }

    @Override
    public List<Shop> queryNearby(Long typeId, BigDecimal x, BigDecimal y, int current) {
        if (typeId == null || x == null || y == null) {
            throw new BizException("附近商户查询参数不完整");
        }
        int pageNo = Math.max(current, 1);
        int pageSize = appProperties.getGeo().getNearbyPageSize();
        int from = (pageNo - 1) * pageSize;
        int end = from + pageSize;
        // GEO 查询先取到当前页末尾，再在内存中截取当前页，保证距离排序与分页结果保持一致。
        GeoOperations<String, String> geoOperations = stringRedisTemplate.opsForGeo();
        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .limit(end);
        var results = geoOperations.search(
                RedisConstants.SHOP_GEO_KEY + typeId,
                GeoReference.fromCoordinate(new Point(x.doubleValue(), y.doubleValue())),
                new Distance(5, Metrics.KILOMETERS),
                args
        );
        if (results == null || results.getContent().size() <= from) {
            return List.of();
        }

        List<Long> shopIds = new ArrayList<>();
        Map<Long, Double> distanceMap = new LinkedHashMap<>();
        results.getContent().stream().skip(from).forEach(result -> {
            Long shopId = Long.valueOf(Objects.requireNonNull(result.getContent().getName()).toString());
            shopIds.add(shopId);
            distanceMap.put(shopId, result.getDistance() == null ? null : result.getDistance().getValue());
        });

        List<Shop> shops = shopMapper.selectBatchIds(shopIds);
        Map<Long, Shop> shopMap = shops.stream().collect(Collectors.toMap(Shop::getId, item -> item));
        List<Shop> ordered = new ArrayList<>();
        // 批量查库返回顺序不保证与 GEO 结果一致，这里按 shopIds 重新组装，避免距离与商户顺序错位。
        for (Long shopId : shopIds) {
            Shop shop = shopMap.get(shopId);
            if (shop != null) {
                shop.setDistance(distanceMap.get(shopId));
                ordered.add(shop);
            }
        }
        log.info("附近商户查询完成，typeId={}, count={}", typeId, ordered.size());
        return ordered;
    }
}
