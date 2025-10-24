package com.example.dataquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dataquery.entity.EventLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件日志Mapper
 */
@Mapper
public interface EventLogMapper extends BaseMapper<EventLog> {
}
