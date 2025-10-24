package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.EventLogResponse;
import com.example.dataquery.entity.EventLog;
import com.example.dataquery.mapper.EventLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 事件日志服务
 */
@Slf4j
@Service
public class EventLogService extends ServiceImpl<EventLogMapper, EventLog> {

    /**
     * 分页查询事件日志
     */
    public IPage<EventLogResponse> listEventLogs(PageRequest pageRequest,
                                                   String eventType,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   String executeStatus) {
        Page<EventLog> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<EventLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(eventType), EventLog::getEventType, eventType);
        queryWrapper.ge(startTime != null, EventLog::getCreateTime, startTime);
        queryWrapper.le(endTime != null, EventLog::getCreateTime, endTime);
        queryWrapper.eq(StringUtils.isNotBlank(executeStatus),EventLog::getExecuteStatus, executeStatus);
        queryWrapper.orderByDesc(EventLog::getCreateTime);

        IPage<EventLog> logPage = this.page(page, queryWrapper);

        IPage<EventLogResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(logPage, responsePage);

        List<EventLogResponse> responseList = logPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 转换为响应DTO
     */
    private EventLogResponse convertToResponse(EventLog log) {
        EventLogResponse response = new EventLogResponse();
        BeanUtils.copyProperties(log, response);
        return response;
    }
}
