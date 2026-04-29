package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.ShopTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopTypeServiceImpl implements ShopTypeService {

    private final ShopTypeMapper shopTypeMapper;

    @Override
    public List<ShopType> listAll() {
        // 分类列表是公开基础数据，按 sort 再按 id 稳定排序，保证前后端展示顺序一致。
        return shopTypeMapper.selectList(new QueryWrapper<ShopType>().orderByAsc("sort", "id"));
    }
}
