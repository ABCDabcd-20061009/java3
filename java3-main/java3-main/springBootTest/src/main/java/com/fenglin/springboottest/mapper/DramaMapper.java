package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Drama;
import org.apache.ibatis.annotations.Mapper;

/** 广播剧内容表 t_drama 的 Mapper（零 XML，继承 BaseMapper） */
@Mapper
public interface DramaMapper extends BaseMapper<Drama> {
}
