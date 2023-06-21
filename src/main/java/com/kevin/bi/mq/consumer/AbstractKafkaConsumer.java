package com.kevin.bi.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Iterator;
import java.util.List;

/**
 * @author: kevin-senzou
 **/
@Slf4j
public abstract class AbstractKafkaConsumer {

    public abstract void execute(String message);


    public void consume(List<ConsumerRecord<String,String>> consumerRecords, Acknowledgment ack){
        log.info("KafkaConsumerExample:consume--> consumerRecords size:{}",consumerRecords.size());
        Iterator<ConsumerRecord<String, String>> it = consumerRecords.iterator();
        while (it.hasNext()){
            ConsumerRecord<String, String> consumerRecord = it.next();
            if(consumerRecord == null){
                break;
            }
            log.info("msg: {}, topic:{}, offset:{}",
                    consumerRecord.value(),consumerRecord.topic(),consumerRecord.offset());
            execute(consumerRecord.value());
        }
        ack.acknowledge();
    }
}
