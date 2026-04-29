package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.service.ShopService;
import com.hmdp.service.ShopTypeService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ShopTypeService shopTypeService;

    @GetMapping
    public Result<List<Shop>> queryShops(@RequestParam(required = false) Long typeId,
                                         @RequestParam(defaultValue = "1") @Min(1) int page) {
        return Result.ok(shopService.queryByType(typeId, page));
    }

    @GetMapping("/{shopId}")
    public Result<Shop> queryShopDetail(@PathVariable Long shopId) {
        return Result.ok(shopService.queryById(shopId));
    }

    @GetMapping("/nearby")
    // 附近商户属于公开浏览能力，但依赖前端传入坐标参数，后端仍需校验基础范围与分页参数。
    public Result<List<Shop>> queryNearby(@RequestParam Long typeId,
                                          @RequestParam @DecimalMin("0.0") BigDecimal x,
                                          @RequestParam @DecimalMin("0.0") BigDecimal y,
                                          @RequestParam(defaultValue = "1") @Min(1) int current) {
        return Result.ok(shopService.queryNearby(typeId, x, y, current));
    }

    @GetMapping("/types")
    public Result<List<ShopType>> queryTypes() {
        return Result.ok(shopTypeService.listAll());
    }
}
