package com.example.dataquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dataquery.entity.ScheduleJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度任务Mapper
 */
@Mapper
public interface ScheduleJobMapper extends BaseMapper<ScheduleJob> {
}
