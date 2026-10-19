package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DramaCategory;
import org.apache.ibatis.annotations.Mapper;

/** 广播剧分类字典表 t_drama_category 的 Mapper（零 XML，继承 BaseMapper） */
@Mapper
public interface DramaCategoryMapper extends BaseMapper<DramaCategory> {
}
