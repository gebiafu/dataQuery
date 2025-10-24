package com.example.dataquery.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.DataSourceRequest;
import com.example.dataquery.dto.DataSourceResponse;
import com.example.dataquery.entity.DataSource;
import com.example.dataquery.entity.SysUser;
import com.example.dataquery.exception.BusinessException;
import com.example.dataquery.mapper.DataSourceMapper;
import com.example.dataquery.mapper.SysUserMapper;
import com.example.dataquery.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据源服务
 */
@Slf4j
@Service
public class DataSourceService extends ServiceImpl<DataSourceMapper, DataSource> {

    // 数据源连接池缓存
    private final Map<Long, DruidDataSource> dataSourcePool = new ConcurrentHashMap<>();

    private final SysUserMapper sysUserMapper;

    public DataSourceService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

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
     * 创建数据源
     */
    public void createDataSource(DataSourceRequest request) {
        // 测试连接
        testConnection(request);

        DataSource dataSource = new DataSource();
        BeanUtils.copyProperties(request, dataSource);
        dataSource.setCreateUserId(UserContext.getUserId());
        dataSource.setStatus(1);

        this.save(dataSource);
    }

    /**
     * 更新数据源
     */
    public void updateDataSource(DataSourceRequest request) {
        DataSource dataSource = this.getById(request.getId());
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 数据隔离：非管理员只能修改自己创建的数据源
        if (!isAdmin() && !dataSource.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限修改该数据源");
        }

        // 测试连接
        testConnection(request);

        BeanUtils.copyProperties(request, dataSource);
        this.updateById(dataSource);

        // 清除缓存
        removeDataSourceFromPool(request.getId());
    }

    /**
     * 删除数据源
     */
    public void deleteDataSource(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 数据隔离：非管理员只能删除自己创建的数据源
        if (!isAdmin() && !dataSource.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限删除该数据源");
        }

        this.removeById(id);
        removeDataSourceFromPool(id);
    }

    /**
     * 查询数据源列表
     */
    public IPage<DataSourceResponse> listDataSources(PageRequest pageRequest, String name, String type) {
        Page<DataSource> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), DataSource::getName, name);
        queryWrapper.eq(StringUtils.isNotBlank(type), DataSource::getType, type);

//        // 数据隔离：非管理员只能查看自己创建的数据源
//        if (!isAdmin()) {
//            queryWrapper.eq(DataSource::getCreateUserId, UserContext.getUserId());
//        }

        queryWrapper.orderByDesc(DataSource::getCreateTime);

        IPage<DataSource> dataSourcePage = this.page(page, queryWrapper);

        IPage<DataSourceResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(dataSourcePage, responsePage);

        List<DataSourceResponse> responseList = dataSourcePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 查询所有数据源（用于下拉选择）
     */
    public List<DataSourceResponse> listAllDataSources() {
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataSource::getStatus, 1);

        // 数据隔离：非管理员只能查看自己创建的数据源
//        if (!isAdmin()) {
//            queryWrapper.eq(DataSource::getCreateUserId, UserContext.getUserId());
//        }

        queryWrapper.orderByDesc(DataSource::getCreateTime);

        return this.list(queryWrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取数据源详情
     */
    public DataSourceResponse getDataSourceById(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new BusinessException("数据源不存在");
        }

        // 数据隔离：非管理员只能查看自己创建的数据源
        if (!isAdmin() && !dataSource.getCreateUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权限查看该数据源");
        }

        return convertToResponse(dataSource);
    }

    /**
     * 测试数据源连接
     */
    public void testConnection(DataSourceRequest request) {
        DruidDataSource druidDataSource = createDruidDataSource(request);
        try (Connection connection = druidDataSource.getConnection()) {
            log.info("数据源连接测试成功：{}", request.getName());
        } catch (SQLException e) {
            log.error("数据源连接测试失败：{}", request.getName(), e);
            throw new BusinessException("数据源连接失败：" + e.getMessage());
        } finally {
            druidDataSource.close();
        }
    }

    /**
     * 获取数据源连接
     */
    public Connection getConnection(Long dataSourceId) {
        DruidDataSource druidDataSource = dataSourcePool.get(dataSourceId);

        if (druidDataSource == null) {
            synchronized (this) {
                druidDataSource = dataSourcePool.get(dataSourceId);
                if (druidDataSource == null) {
                    DataSource dataSource = this.getById(dataSourceId);
                    if (dataSource == null) {
                        throw new BusinessException("数据源不存在");
                    }

                    DataSourceRequest request = new DataSourceRequest();
                    BeanUtils.copyProperties(dataSource, request);
                    druidDataSource = createDruidDataSource(request);
                    dataSourcePool.put(dataSourceId, druidDataSource);
                }
            }
        }

        try {
            return druidDataSource.getConnection();
        } catch (SQLException e) {
            log.error("获取数据源连接失败", e);
            throw new BusinessException("获取数据源连接失败：" + e.getMessage());
        }
    }

    /**
     * 创建Druid数据源
     */
    private DruidDataSource createDruidDataSource(DataSourceRequest request) {
        DruidDataSource druidDataSource = new DruidDataSource();

        String jdbcUrl;
        String driverClassName;

        if ("mysql".equalsIgnoreCase(request.getType())) {
            jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai",
                    request.getHost(), request.getPort(), request.getDatabaseName());
            driverClassName = "com.mysql.cj.jdbc.Driver";
        } else if ("postgresql".equalsIgnoreCase(request.getType())) {
            jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                    request.getHost(), request.getPort(), request.getDatabaseName());
            driverClassName = "org.postgresql.Driver";
        } else {
            throw new BusinessException("不支持的数据源类型：" + request.getType());
        }

        druidDataSource.setUrl(jdbcUrl);
        druidDataSource.setDriverClassName(driverClassName);
        druidDataSource.setUsername(request.getUsername());
        druidDataSource.setPassword(request.getPassword());
        druidDataSource.setInitialSize(1);
        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxActive(5);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setValidationQuery("SELECT 1");

        return druidDataSource;
    }

    /**
     * 从连接池中移除数据源
     */
    private void removeDataSourceFromPool(Long dataSourceId) {
        DruidDataSource druidDataSource = dataSourcePool.remove(dataSourceId);
        if (druidDataSource != null) {
            druidDataSource.close();
        }
    }

    /**
     * 转换为响应DTO
     */
    private DataSourceResponse convertToResponse(DataSource dataSource) {
        DataSourceResponse response = new DataSourceResponse();
        BeanUtils.copyProperties(dataSource, response);
        return response;
    }
}
