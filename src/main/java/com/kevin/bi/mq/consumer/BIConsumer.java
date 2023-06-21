package com.kevin.bi.mq.consumer;

import com.kevin.bi.common.ErrorCode;
import com.kevin.bi.exception.ThrowUtils;
import com.kevin.bi.manager.AiManager;
import com.kevin.bi.model.entity.Chart;
import com.kevin.bi.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.kevin.bi.constant.CommonConstant.BI_MODEL_ID;
import static com.kevin.bi.constant.KafkaConstant.KEVIN_BI_GROUP;
import static com.kevin.bi.constant.KafkaConstant.KEVIN_BI_KAFKA_TOPIC;

/**
 * @author: kevin-senzou
 **/
@Component
@Slf4j
public class BIConsumer extends AbstractKafkaConsumer  {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @KafkaListener(groupId = KEVIN_BI_GROUP,topics = KEVIN_BI_KAFKA_TOPIC
            ,containerFactory = "batchFactory",concurrency = "1")
    public void receiveMsg(List<ConsumerRecord<String,String>> consumerRecords, Acknowledgment ack){
        consume(consumerRecords,ack);
    }

    @Override
    public void execute(String message) {
        //将message转为charId
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            log.error("BIConsumer:execute--> chart is null");
            return;
        }
        //先将图表任务状态改为running(执行中)，等成功后，修改为success（已完成）
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if(!b){
            handleChartUpdateError(chart.getId(),"更新图表执行中状态失败");
            return;
        }
        //调用AI
        String result = aiManager.doChat(BI_MODEL_ID,buildUserInput(chart));
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            handleChartUpdateError(chart.getId(), "AI 生成错误");
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("success");
        //修改状态为success
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
    }

    private void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("fail");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            log.error("更新图表失败状态失败",chartId+","+execMessage);
        }
    }

    /**
     * 构造用户输入（给AI用）
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();

    }
}
