package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.DataSourceRequest;
import com.example.dataquery.dto.DataSourceResponse;
import com.example.dataquery.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 创建数据源
     */
    @PostMapping
    public Result<String> create(@Validated @RequestBody DataSourceRequest request) {
        dataSourceService.createDataSource(request);
        return Result.success("创建成功");
    }

    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Validated @RequestBody DataSourceRequest request) {
        request.setId(id);
        dataSourceService.updateDataSource(request);
        return Result.success("更新成功");
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询数据源列表
     */
    @GetMapping("/page")
    public Result<IPage<DataSourceResponse>> page(PageRequest pageRequest,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String type) {
        IPage<DataSourceResponse> page = dataSourceService.listDataSources(pageRequest, name, type);
        return Result.success(page);
    }

    /**
     * 查询所有数据源
     */
    @GetMapping("/list")
    public Result<List<DataSourceResponse>> list() {
        List<DataSourceResponse> list = dataSourceService.listAllDataSources();
        return Result.success(list);
    }

    /**
     * 获取数据源详情
     */
    @GetMapping("/{id}")
    public Result<DataSourceResponse> getById(@PathVariable Long id) {
        DataSourceResponse response = dataSourceService.getDataSourceById(id);
        return Result.success(response);
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test")
    public Result<String> test(@Validated @RequestBody DataSourceRequest request) {
        dataSourceService.testConnection(request);
        return Result.success("连接成功");
    }
}
