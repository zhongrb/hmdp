package com.hmdp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.service.VoucherOrderService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderService voucherOrderService;

    @GetMapping("/seckill")
    // 活动列表允许匿名先浏览，前端据此决定是否引导登录，而真正的抢券动作仍由受保护接口控制。
    public Result<List<Voucher>> querySeckillVouchers() {
        List<Voucher> vouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                .orderByDesc(Voucher::getStatus)
                .orderByAsc(Voucher::getBeginTime)
                .orderByAsc(Voucher::getId));
        LocalDateTime now = LocalDateTime.now();
        vouchers.forEach(voucher -> voucher.setStatus(resolveStatus(voucher, now)));
        return Result.ok(vouchers);
    }

    @PostMapping("/seckill/{voucherId}/claim")
    public Result<Long> claimSeckillVoucher(@PathVariable Long voucherId) {
        return Result.ok(voucherOrderService.claimSeckillVoucher(voucherId));
    }

    private int resolveStatus(Voucher voucher, LocalDateTime now) {
        // 统一把数据库原始字段折算成前端可直接消费的活动状态，避免页面重复拼装时间与库存判断。
        if (voucher.getStock() == null || voucher.getStock() <= 0) {
            return 3;
        }
        if (voucher.getBeginTime().isAfter(now)) {
            return 0;
        }
        if (voucher.getEndTime().isBefore(now)) {
            return 2;
        }
        return 1;
    }
}
