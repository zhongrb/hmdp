package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    @Update("""
            update tb_voucher
            set stock = stock - 1
            where id = #{voucherId}
              and stock > 0
            """)
    int deductStock(@Param("voucherId") Long voucherId);
}
