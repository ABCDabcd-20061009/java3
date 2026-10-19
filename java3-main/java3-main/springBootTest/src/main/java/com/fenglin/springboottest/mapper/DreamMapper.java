package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Dream;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DreamMapper extends BaseMapper<Dream> {
}
