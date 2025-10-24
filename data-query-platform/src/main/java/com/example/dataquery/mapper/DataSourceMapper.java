package com.example.dataquery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dataquery.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源Mapper
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {
}
