package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.ScheduleLogResponse;
import com.example.dataquery.entity.ScheduleLog;
import com.example.dataquery.mapper.ScheduleLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度日志服务
 */
@Slf4j
@Service
public class ScheduleLogService extends ServiceImpl<ScheduleLogMapper, ScheduleLog> {

    /**
     * 分页查询调度日志
     */
    public IPage<ScheduleLogResponse> listScheduleLogs(PageRequest pageRequest,
                                                         Long jobId,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         String executeStatus) {
        Page<ScheduleLog> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<ScheduleLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(String.valueOf(jobId)), ScheduleLog::getJobId, jobId);
        queryWrapper.ge(startTime != null, ScheduleLog::getCreateTime, startTime);
        queryWrapper.le(endTime != null, ScheduleLog::getCreateTime, endTime);
        queryWrapper.eq(StringUtils.isNotBlank(executeStatus), ScheduleLog::getExecuteStatus, executeStatus);
        queryWrapper.orderByDesc(ScheduleLog::getCreateTime);

        IPage<ScheduleLog> logPage = this.page(page, queryWrapper);

        IPage<ScheduleLogResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(logPage, responsePage);

        List<ScheduleLogResponse> responseList = logPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 转换为响应DTO
     */
    private ScheduleLogResponse convertToResponse(ScheduleLog log) {
        ScheduleLogResponse response = new ScheduleLogResponse();
        BeanUtils.copyProperties(log, response);
        return response;
    }
}
