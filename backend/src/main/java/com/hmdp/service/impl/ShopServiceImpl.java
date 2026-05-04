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
import org.springframework.transaction.annotation.Transactional;

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
        Shop shop = cacheClient.queryWithLogicalExpire(
                RedisConstants.CACHE_SHOP_KEY,
                RedisConstants.LOCK_SHOP_KEY,
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
    @Transactional
    public Shop createShop(Shop shop) {
        validateShop(shop);
        shop.setId(null);
        shopMapper.insert(shop);
        try {
            writeShopGeo(shop);
            deleteShopCache(shop.getId());
            log.info("商户新增成功，shopId={}, typeId={}", shop.getId(), shop.getTypeId());
            return shop;
        } catch (Exception exception) {
            log.error("商户新增后的缓存或GEO维护失败，shopId={}", shop.getId(), exception);
            throw new BizException("商户新增后缓存同步失败");
        }
    }

    @Override
    @Transactional
    public Shop updateShop(Long shopId, Shop shop) {
        Shop existing = requireShop(shopId);
        applyUpdate(shopId, shop);
        Shop updated = requireShop(shopId);
        try {
            refreshShopGeo(existing, updated);
            deleteShopCache(shopId);
            log.info("商户更新成功，shopId={}", shopId);
            return updated;
        } catch (Exception exception) {
            log.error("商户更新后的缓存或GEO维护失败，shopId={}", shopId, exception);
            throw new BizException("商户更新后缓存同步失败");
        }
    }

    @Override
    @Transactional
    public void deleteShop(Long shopId) {
        Shop existing = requireShop(shopId);
        shopMapper.deleteById(shopId);
        try {
            removeShopGeo(existing);
            deleteShopCache(shopId);
            log.info("商户删除成功，shopId={}", shopId);
        } catch (Exception exception) {
            log.error("商户删除后的缓存或GEO清理失败，shopId={}", shopId, exception);
            throw new BizException("商户删除后缓存同步失败");
        }
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

    private void validateShop(Shop shop) {
        if (shop == null || shop.getTypeId() == null || shop.getName() == null || shop.getName().isBlank()
                || shop.getAddress() == null || shop.getAddress().isBlank() || shop.getX() == null || shop.getY() == null) {
            throw new BizException("商户参数不完整");
        }
    }

    private Shop requireShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException("商户不存在");
        }
        return shop;
    }

    private void applyUpdate(Long shopId, Shop shop) {
        validateShop(shop);
        shop.setId(shopId);
        shopMapper.updateById(shop);
    }

    private void refreshShopGeo(Shop oldShop, Shop newShop) {
        if (!Objects.equals(oldShop.getTypeId(), newShop.getTypeId())) {
            removeShopGeo(oldShop);
            writeShopGeo(newShop);
            log.info("商户GEO类型索引已迁移，shopId={}, oldTypeId={}, newTypeId={}", newShop.getId(), oldShop.getTypeId(), newShop.getTypeId());
            return;
        }
        if (!Objects.equals(oldShop.getX(), newShop.getX()) || !Objects.equals(oldShop.getY(), newShop.getY())) {
            writeShopGeo(newShop);
            log.info("商户GEO坐标已更新，shopId={}", newShop.getId());
        }
    }

    private void writeShopGeo(Shop shop) {
        stringRedisTemplate.opsForGeo().add(
                RedisConstants.SHOP_GEO_KEY + shop.getTypeId(),
                new Point(shop.getX().doubleValue(), shop.getY().doubleValue()),
                String.valueOf(shop.getId())
        );
        log.info("商户GEO索引写入完成，shopId={}, typeId={}", shop.getId(), shop.getTypeId());
    }

    private void removeShopGeo(Shop shop) {
        stringRedisTemplate.opsForGeo().remove(
                RedisConstants.SHOP_GEO_KEY + shop.getTypeId(),
                String.valueOf(shop.getId())
        );
        log.info("商户GEO索引删除完成，shopId={}, typeId={}", shop.getId(), shop.getTypeId());
    }

    private void deleteShopCache(Long shopId) {
        cacheClient.delete(RedisConstants.CACHE_SHOP_KEY + shopId);
        log.info("商户详情缓存删除完成，shopId={}", shopId);
    }
}
