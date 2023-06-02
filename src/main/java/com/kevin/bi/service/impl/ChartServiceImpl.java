package com.kevin.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.bi.model.entity.Chart;
import com.kevin.bi.service.ChartService;
import com.kevin.bi.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author zousen
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-05-18 15:52:42
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




