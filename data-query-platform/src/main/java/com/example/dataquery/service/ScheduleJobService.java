package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.ScheduleJobRequest;
import com.example.dataquery.dto.ScheduleJobResponse;
import com.example.dataquery.entity.QueryEvent;
import com.example.dataquery.entity.ScheduleJob;
import com.example.dataquery.entity.SysUser;
import com.example.dataquery.exception.BusinessException;
import com.example.dataquery.mapper.ScheduleJobMapper;
import com.example.dataquery.mapper.SysUserMapper;
import com.example.dataquery.quartz.DataQueryJob;
import com.example.dataquery.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度任务服务
 */
@Slf4j
@Service
public class ScheduleJobService extends ServiceImpl<ScheduleJobMapper, ScheduleJob> {

    @Resource
    private Scheduler scheduler;

    @Resource
    private QueryEventService queryEventService;
    
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
     * 创建调度任务
     */
    public void createJob(ScheduleJobRequest request) {
        // 验证Cron表达式
        if (!CronExpression.isValidExpression(request.getCronExpression())) {
            throw new BusinessException("Cron表达式格式错误");
        }

        // 验证事件是否存在
        QueryEvent event = queryEventService.getById(request.getEventId());
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        ScheduleJob job = new ScheduleJob();
        BeanUtils.copyProperties(request, job);
        job.setCreateUserId(UserContext.getUserId());
        job.setJobStatus("STOP");

        this.save(job);
    }

    /**
     * 更新调度任务
     */
    public void updateJob(ScheduleJobRequest request) {
        ScheduleJob job = this.getById(request.getId());
        if (job == null) {
            throw new BusinessException("任务不存在");
        }

        // 验证Cron表达式
        if (!CronExpression.isValidExpression(request.getCronExpression())) {
            throw new BusinessException("Cron表达式格式错误");
        }

        // 验证事件是否存在
        QueryEvent event = queryEventService.getById(request.getEventId());
        if (event == null) {
            throw new BusinessException("事件不存在");
        }

        // 如果任务正在运行，需要先停止
        if ("RUNNING".equals(job.getJobStatus())) {
            stopJob(job.getId());
        }

        BeanUtils.copyProperties(request, job);
        this.updateById(job);
    }

    /**
     * 删除调度任务
     */
    public void deleteJob(Long id) {
        ScheduleJob job = this.getById(id);
        if (job != null && "RUNNING".equals(job.getJobStatus())) {
            stopJob(id);
        }
        this.removeById(id);
    }

    /**
     * 启动任务
     */
    public void startJob(Long jobId) {
        ScheduleJob job = this.getById(jobId);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }

        if ("RUNNING".equals(job.getJobStatus())) {
            throw new BusinessException("任务已在运行中");
        }

        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(DataQueryJob.class)
                    .withIdentity("job_" + jobId, "DATA_QUERY")
                    .build();

            jobDetail.getJobDataMap().put("jobId", jobId);
            jobDetail.getJobDataMap().put("jobName", job.getJobName());
            jobDetail.getJobDataMap().put("eventId", job.getEventId());

            // 创建Trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + jobId, "DATA_QUERY")
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            // 更新任务状态
            job.setJobStatus("RUNNING");
            this.updateById(job);

            log.info("启动调度任务成功：jobId={}, jobName={}", jobId, job.getJobName());

        } catch (SchedulerException e) {
            log.error("启动调度任务失败：jobId=" + jobId, e);
            throw new BusinessException("启动任务失败：" + e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    public void stopJob(Long jobId) {
        ScheduleJob job = this.getById(jobId);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }

        if ("STOP".equals(job.getJobStatus())) {
            throw new BusinessException("任务未运行");
        }

        try {
            JobKey jobKey = JobKey.jobKey("job_" + jobId, "DATA_QUERY");
            scheduler.deleteJob(jobKey);

            // 更新任务状态
            job.setJobStatus("STOP");
            this.updateById(job);

            log.info("停止调度任务成功：jobId={}, jobName={}", jobId, job.getJobName());

        } catch (SchedulerException e) {
            log.error("停止调度任务失败：jobId=" + jobId, e);
            throw new BusinessException("停止任务失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询调度任务列表
     */
    public IPage<ScheduleJobResponse> listJobs(PageRequest pageRequest, String jobName, String jobStatus) {
        Page<ScheduleJob> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<ScheduleJob> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(jobName), ScheduleJob::getJobName, jobName);
        queryWrapper.eq(StringUtils.isNotBlank(jobStatus), ScheduleJob::getJobStatus, jobStatus);
        
        // 数据隔离：非管理员只能查看自己创建的任务
        if (!isAdmin()) {
            queryWrapper.eq(ScheduleJob::getCreateUserId, UserContext.getUserId());
        }
        
        queryWrapper.orderByDesc(ScheduleJob::getCreateTime);

        IPage<ScheduleJob> jobPage = this.page(page, queryWrapper);

        IPage<ScheduleJobResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(jobPage, responsePage);

        List<ScheduleJobResponse> responseList = jobPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 获取调度任务详情
     */
    public ScheduleJobResponse getJobById(Long id) {
        ScheduleJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }
        return convertToResponse(job);
    }

    /**
     * 转换为响应DTO
     */
    private ScheduleJobResponse convertToResponse(ScheduleJob job) {
        ScheduleJobResponse response = new ScheduleJobResponse();
        BeanUtils.copyProperties(job, response);

        // 获取事件名称
        QueryEvent event = queryEventService.getById(job.getEventId());
        if (event != null) {
            response.setEventName(event.getName());
        }

        return response;
    }
}
