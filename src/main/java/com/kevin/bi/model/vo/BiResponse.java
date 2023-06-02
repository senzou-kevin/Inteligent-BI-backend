package com.kevin.bi.model.vo;

import lombok.Data;

/**
 * BI的返回结果
 * @author zousen
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    private Long chartId;
}
