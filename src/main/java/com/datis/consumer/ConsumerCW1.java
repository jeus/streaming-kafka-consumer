/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datis.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

/**
 *
 * @author jeus
 */
public class ConsumerCW1 extends Thread {

    private boolean logOn = true;
    List<TopicPartition> tp = new ArrayList<>(4);
    private static int[] partitionNumb = new int[4];
    private static long[][] minMaxOffset = new long[4][2];
    KafkaConsumer consumer;
    static Properties pro = new Properties();
    String topic = "";

    public ConsumerCW1() {
    }

    public ConsumerCW1(String topic, Properties props, boolean logOn) {
        this.logOn = logOn;
        consumer = new KafkaConsumer(props);
        this.topic = topic;
        tp.add(new TopicPartition(topic, 0));

    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singleton(topic));//subscribe all topics for poll
//        while (true) {
        System.out.println("Consumer Have to poll Consumer position:");
//            consumer.assign(partition);
//            consumer.seekToBeginning(Collections.singleton(tp0));
//            consumer.seek(tp0,100);
        consumer.poll(100);
        Set<TopicPartition> setTopic = consumer.assignment();
        System.out.println("SIIIIIZE" + setTopic.size());
        for (TopicPartition topicPartition : setTopic) {
            System.out.println("topic and partition:" + topicPartition.toString());
        }

        consumer.seek(tp.get(0), 1);
        while (logOn) {

            ConsumerRecords<String, Long> records = consumer.poll(10000);
            consumer.commitSync();
            System.out.println(logPosition());
//            System.out.println( logPosition());
            for (ConsumerRecord<String, Long> rec : records) {
                if (logOn) {
//                System.out.println("RECORD IS SAVED:(Key:" + rec.key() + ")    (Value:" + rec.value() + ")     (Partition:" + rec.partition() + ":" + rec.offset() + ")   (TimeStamp:" + rec.timestamp() + ")");
                    System.out.println("(Key:" + rec.key() + ")    (Value:" + rec.value() + ")");
                }
                partitionBenchMark(rec);
            }
        }

//        }
    }

    public void loger() {
        long sum = 0;
        for (int i = 0; i < 4; i++) {

            sum = sum + partitionNumb[i];
            System.out.println("=============Partiotion" + i + "=============");
            System.out.println("=============" + partitionNumb[i] + " (Min:" + minMaxOffset[i][0] + " Max:" + minMaxOffset[i][1] + ")");
            System.out.println("=============Partiotion" + i + "=============\n");
        }
        System.out.println("********SUM********");
        System.out.println("********" + sum + "********");
    }

    private String logPosition() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1; i++) {
            try {
                TopicPartition tp = new TopicPartition(topic, i);
                sb.append("(p" + i + " :" + consumer.position(tp) + ")");
            } catch (Exception e) {
                System.err.println("Partition:" + i + " ERROR:" + e.getMessage());
            }
        }
        return sb.toString();
    }

    private void partitionBenchMark(ConsumerRecord rec) {
        if (rec != null) {
            partitionNumb[rec.partition()]++;
            if (Long.compare(minMaxOffset[rec.partition()][0], rec.offset()) > 0 || minMaxOffset[rec.partition()][0] == 0) {
                minMaxOffset[rec.partition()][0] = rec.offset();
            } else if (Long.compare(minMaxOffset[rec.partition()][1], rec.offset()) < 0) {
                minMaxOffset[rec.partition()][1] = rec.offset();
            }
        }
    }

    public static void main(String[] arg) {
        Properties props = new Properties();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.0.11:2181");//172.17.0.8:2181,172.17.0.9:2181,172.17.0.10:2181");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.0.13:9092");//172.17.0.8:2181,172.17.0.9:2181,172.17.0.10:2181");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "TestMikonam1");
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, (4000 * 10000) + "");//change this for increase and decrease packet fethe by consumer every message is 100Byte
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_DOC, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongDeserializer");
        ConsumerCW1 consumer1 = new ConsumerCW1("cw1", props, true);
        consumer1.run();
    }
}
