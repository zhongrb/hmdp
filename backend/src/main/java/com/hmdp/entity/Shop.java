package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_shop")
public class Shop extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long typeId;
    private String address;
    private BigDecimal x;
    private BigDecimal y;
    private String images;
    private BigDecimal avgPrice;
    private Integer comments;
    private BigDecimal score;
    private String openHours;

    @TableField(exist = false)
    private Double distance;
}
