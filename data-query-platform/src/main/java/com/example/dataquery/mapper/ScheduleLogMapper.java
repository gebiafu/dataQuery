package com.example.dataquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dataquery.entity.ScheduleLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度日志Mapper
 */
@Mapper
public interface ScheduleLogMapper extends BaseMapper<ScheduleLog> {
}
