package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_shop_type")
public class ShopType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sort;
    private String icon;
}
