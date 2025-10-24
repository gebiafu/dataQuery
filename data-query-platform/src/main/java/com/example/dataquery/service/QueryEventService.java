package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.QueryEventRequest;
import com.example.dataquery.dto.QueryEventResponse;
import com.example.dataquery.dto.SqlExecuteRequest;
import com.example.dataquery.dto.SqlExecuteResponse;
import com.example.dataquery.entity.DataSource;
import com.example.dataquery.entity.EventLog;
import com.example.dataquery.entity.QueryEvent;
import com.example.dataquery.entity.SysUser;
import com.example.dataquery.exception.BusinessException;
import com.example.dataquery.mapper.QueryEventMapper;
import com.example.dataquery.mapper.SysUserMapper;
import com.example.dataquery.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询事件服务
 */
@Slf4j
@Service
public class QueryEventService extends ServiceImpl<QueryEventMapper, QueryEvent> {

    @Resource
    private DataSourceService dataSourceService;

    @Resource
    private EventLogService eventLogService;
    
    @Resource
    private SysUserMapper sysUserMapper;
    
    /**
     * 检查是否为管理员
     */
    private boolean isAdmin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 创建事件
     */
    public void createEvent(QueryEventRequest request) {
        QueryEvent event = new QueryEvent();
        BeanUtils.copyProperties(request, event);
        event.setCreateUserId(UserContext.getUserId());
        event.setStatus(1);

        this.save(event);
    }

    /**
     * 更新事件
     */
    public void updateEvent(QueryEventRequest request) {
        QueryEvent event = this.getById(request.getId());
        if (event == null) {
            throw new BusinessException("事件不存在");
        }
        
        // 数据隔离：非管理员只能修改自己创建的事件
        if (!isAdmin() && !event.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限修改该事件");
        }

        BeanUtils.copyProperties(request, event);
        this.updateById(event);
    }

    /**
     * 删除事件
     */
    public void deleteEvent(Long id) {
        QueryEvent event = this.getById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }
        
        // 数据隔离：非管理员只能删除自己创建的事件
        if (!isAdmin() && !event.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限删除该事件");
        }
        
        this.removeById(id);
    }

    /**
     * 分页查询事件列表
     */
    public IPage<QueryEventResponse> listEvents(PageRequest pageRequest, String name, String eventType, Long dataSourceId) {
        Page<QueryEvent> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<QueryEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), QueryEvent::getName, name);
        queryWrapper.eq(StringUtils.isNotBlank(eventType), QueryEvent::getEventType, eventType);
        queryWrapper.eq(dataSourceId != null, QueryEvent::getDataSourceId, dataSourceId);
        
        // 数据隔离：非管理员只能查看自己创建的事件
        if (!isAdmin()) {
            queryWrapper.eq(QueryEvent::getCreateUserId, UserContext.getUserId());
        }
        
        queryWrapper.orderByDesc(QueryEvent::getCreateTime);

        IPage<QueryEvent> eventPage = this.page(page, queryWrapper);

        IPage<QueryEventResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(eventPage, responsePage);

        List<QueryEventResponse> responseList = eventPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 查询所有事件（用于下拉选择）
     */
    public List<QueryEventResponse> listAllEvents() {
        LambdaQueryWrapper<QueryEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QueryEvent::getStatus, 1);
        
        // 数据隔离：非管理员只能查看自己创建的事件
        if (!isAdmin()) {
            queryWrapper.eq(QueryEvent::getCreateUserId, UserContext.getUserId());
        }
        
        queryWrapper.orderByDesc(QueryEvent::getCreateTime);

        return this.list(queryWrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取事件详情
     */
    public QueryEventResponse getEventById(Long id) {
        QueryEvent event = this.getById(id);
        if (event == null) {
            throw new BusinessException("事件不存在");
        }
        
        // 数据隔离：非管理员只能查看自己创建的事件
        if (!isAdmin() && !event.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限查看该事件");
        }
        
        return convertToResponse(event);
    }

    /**
     * 执行SQL
     */
    public SqlExecuteResponse executeSql(SqlExecuteRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        SqlExecuteResponse response = new SqlExecuteResponse();
        EventLog eventLog = new EventLog();

        try {
            // 获取数据源信息
            DataSource dataSource = dataSourceService.getById(request.getDataSourceId());
            if (dataSource == null) {
                throw new BusinessException("数据源不存在");
            }

            // 获取事件信息（如果有）
            QueryEvent event = null;
            if (request.getEventId() != null) {
                event = this.getById(request.getEventId());
            }

            // 获取连接并执行SQL
            try (Connection connection = dataSourceService.getConnection(request.getDataSourceId());
                 Statement statement = connection.createStatement()) {

                String sql = request.getSql().trim();
                boolean isQuery = sql.toLowerCase().startsWith("select");

                if (isQuery) {
                    // 查询操作
                    try (ResultSet resultSet = statement.executeQuery(sql)) {
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        // 获取列名
                        List<String> columns = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            columns.add(metaData.getColumnLabel(i));
                        }

                        // 获取数据
                        List<Map<String, Object>> data = new ArrayList<>();
                        while (resultSet.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                            }
                            data.add(row);
                        }

                        response.setSuccess(true);
                        response.setMessage("查询成功");
                        response.setColumns(columns);
                        response.setData(data);
                    }
                } else {
                    // 增删改操作
                    int affectedRows = statement.executeUpdate(sql);

                    response.setSuccess(true);
                    response.setMessage("执行成功");
                    response.setAffectedRows(affectedRows);
                }

                LocalDateTime endTime = LocalDateTime.now();
                long duration = Duration.between(startTime, endTime).toMillis();
                response.setDuration(duration);

                // 记录日志
                eventLog.setExecuteStatus("SUCCESS");
                eventLog.setExecuteResult(response.getMessage());
                eventLog.setDuration(duration);

            }

        } catch (Exception e) {
            log.error("SQL执行失败", e);

            LocalDateTime endTime = LocalDateTime.now();
            long duration = Duration.between(startTime, endTime).toMillis();

            response.setSuccess(false);
            response.setMessage("执行失败：" + e.getMessage());
            response.setDuration(duration);

            // 记录错误日志
            eventLog.setExecuteStatus("FAIL");
            eventLog.setErrorMessage(e.getMessage());
            eventLog.setDuration(duration);
        } finally {
            // 保存执行日志
            eventLog.setStartTime(startTime);
            eventLog.setEndTime(LocalDateTime.now());
            eventLog.setSqlContent(request.getSql());
            eventLog.setDataSourceId(request.getDataSourceId());
            eventLog.setExecuteUserId(UserContext.getUserId());
            eventLog.setExecuteUserName(UserContext.getUsername());

            if (request.getEventId() != null) {
                QueryEvent event = this.getById(request.getEventId());
                if (event != null) {
                    eventLog.setEventId(event.getId());
                    eventLog.setEventName(event.getName());
                    eventLog.setEventType(event.getEventType());
                }
            }

            DataSource dataSource = dataSourceService.getById(request.getDataSourceId());
            if (dataSource != null) {
                eventLog.setDataSourceName(dataSource.getName());
            }

            eventLogService.save(eventLog);
        }

        return response;
    }

    /**
     * 转换为响应DTO
     */
    private QueryEventResponse convertToResponse(QueryEvent event) {
        QueryEventResponse response = new QueryEventResponse();
        BeanUtils.copyProperties(event, response);

        // 获取数据源名称
        DataSource dataSource = dataSourceService.getById(event.getDataSourceId());
        if (dataSource != null) {
            response.setDataSourceName(dataSource.getName());
        }

        return response;
    }
}
