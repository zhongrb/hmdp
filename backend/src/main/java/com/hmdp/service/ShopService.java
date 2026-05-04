package com.hmdp.service;

import com.hmdp.entity.Shop;
import java.math.BigDecimal;
import java.util.List;

public interface ShopService {

    List<Shop> queryByType(Long typeId, int page);

    Shop queryById(Long shopId);

    Shop createShop(Shop shop);

    Shop updateShop(Long shopId, Shop shop);

    void deleteShop(Long shopId);

    List<Shop> queryNearby(Long typeId, BigDecimal x, BigDecimal y, int current);
}
