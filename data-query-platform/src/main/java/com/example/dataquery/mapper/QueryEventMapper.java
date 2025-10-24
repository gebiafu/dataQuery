package com.example.dataquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dataquery.entity.QueryEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 查询事件Mapper
 */
@Mapper
public interface QueryEventMapper extends BaseMapper<QueryEvent> {
}
