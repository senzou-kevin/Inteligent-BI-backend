package com.kevin.bi.mq.producer;

import com.kevin.bi.constant.KafkaConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;

/**
 * @author: kevin-senzou
 **/
@Component
@Slf4j
public class BIProducer {

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;



    public void doSend(String msg){
        ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(KafkaConstant.KEVIN_BI_KAFKA_TOPIC, "kafka-topic-key", msg);
        result.addCallback(success ->{
            String topic = success.getRecordMetadata().topic();
            int partition = success.getRecordMetadata().partition();
            long offset = success.getRecordMetadata().offset();
            ProducerRecord<String, String> producerRecord = success.getProducerRecord();
            log.info("BIProducer:doSend-->message sent successfully, topic is {}, partition is {}, offset is {}, record:{}"
                    ,topic,partition,offset,producerRecord);
        },failure -> {
            log.error("BIProducer:doSend-->fail to send message, error msg: {}", failure.getMessage());
        });
    }
}
