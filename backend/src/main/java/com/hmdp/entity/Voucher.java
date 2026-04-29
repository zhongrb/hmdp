package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_voucher")
public class Voucher extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private String title;
    private String subTitle;
    private Long payValue;
    private Long actualValue;
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer status;
}
