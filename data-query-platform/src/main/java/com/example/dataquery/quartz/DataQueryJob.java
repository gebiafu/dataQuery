package com.example.dataquery.quartz;

import com.example.dataquery.dto.SqlExecuteRequest;
import com.example.dataquery.dto.SqlExecuteResponse;
import com.example.dataquery.entity.QueryEvent;
import com.example.dataquery.entity.ScheduleLog;
import com.example.dataquery.service.QueryEventService;
import com.example.dataquery.service.ScheduleLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 数据查询任务
 */
@Slf4j
@Component
public class DataQueryJob implements Job {
    
    @Autowired
    private QueryEventService queryEventService;
    
    @Autowired
    private ScheduleLogService scheduleLogService;
    
    @Override
    public void execute(JobExecutionContext context) {
        Long jobId = context.getJobDetail().getJobDataMap().getLong("jobId");
        String jobName = context.getJobDetail().getJobDataMap().getString("jobName");
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");
        
        log.info("开始执行调度任务：jobId={}, jobName={}, eventId={}", jobId, jobName, eventId);
        
        LocalDateTime startTime = LocalDateTime.now();
        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setJobId(jobId);
        scheduleLog.setJobName(jobName);
        scheduleLog.setEventId(eventId);
        scheduleLog.setStartTime(startTime);
        
        try {
            // 获取事件信息
            QueryEvent event = queryEventService.getById(eventId);
            if (event == null) {
                throw new Exception("事件不存在：" + eventId);
            }
            
            scheduleLog.setEventName(event.getName());
            
            // 执行SQL
            SqlExecuteRequest request = new SqlExecuteRequest();
            request.setDataSourceId(event.getDataSourceId());
            request.setSql(event.getSqlContent());
            request.setEventId(eventId);
            
            SqlExecuteResponse response = queryEventService.executeSql(request);
            
            LocalDateTime endTime = LocalDateTime.now();
            long duration = Duration.between(startTime, endTime).toMillis();
            
            if (response.getSuccess()) {
                scheduleLog.setExecuteStatus("SUCCESS");
                scheduleLog.setExecuteResult(response.getMessage());
            } else {
                scheduleLog.setExecuteStatus("FAIL");
                scheduleLog.setErrorMessage(response.getMessage());
            }
            
            scheduleLog.setEndTime(endTime);
            scheduleLog.setDuration(duration);
            
            log.info("调度任务执行完成：jobId={}, status={}, duration={}ms", 
                    jobId, scheduleLog.getExecuteStatus(), duration);
            
        } catch (Exception e) {
            log.error("调度任务执行失败：jobId=" + jobId, e);
            
            LocalDateTime endTime = LocalDateTime.now();
            long duration = Duration.between(startTime, endTime).toMillis();
            
            scheduleLog.setExecuteStatus("FAIL");
            scheduleLog.setErrorMessage(e.getMessage());
            scheduleLog.setEndTime(endTime);
            scheduleLog.setDuration(duration);
        } finally {
            // 保存调度日志
            scheduleLogService.save(scheduleLog);
        }
    }
}
