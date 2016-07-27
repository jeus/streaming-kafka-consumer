/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datis.consumer.region;

import com.datis.irc.entity.RegionCount;
import com.datis.irc.entity.WindowedPageViewByRegion;
import com.datis.irc.pojo.JsonPOJODeserializer;
import com.datis.irc.pojo.RegionCountDeserializer;
import com.datis.irc.pojo.WindowDeserializer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

/**
 * start step3 topic that save windowing class POJO
 *
 * @author jeus
 */
public class ConsumerRegionWindowing extends Thread {

    com.datis.irc.pojo.RegionCountDeserializer regCountDeserializer = new RegionCountDeserializer();
    com.datis.irc.pojo.WindowDeserializer wPageViewByRegionDeserializer = new WindowDeserializer();

    private boolean logOn = true;
    List<TopicPartition> tp = new ArrayList<>(4);
    private static int[] partitionNumb = new int[4];
    private static long[][] minMaxOffset = new long[4][2];
    KafkaConsumer consumer;
    static Properties pro = new Properties();
    String topic = "";

    public ConsumerRegionWindowing() {
    }

    public ConsumerRegionWindowing(String topic, Properties props, boolean logOn) {
        this.logOn = logOn;
        consumer = new KafkaConsumer(props);//,wPageViewByRegionDeserializer,regionCountDeserializer);
        this.topic = topic;
        tp.add(new TopicPartition(topic, 0));

    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singleton(topic));//subscribe all topics for poll
        System.out.println("Change It Is work ---------------------*********");

        consumer.poll(100);

        consumer.seek(tp.get(0), 1);
        int position = 8000;
        while (true) {

            ConsumerRecords<WindowedPageViewByRegion, RegionCount> records = consumer.poll(1000);
//            ConsumerRecords<String, String> records = consumer.poll(1000);
            System.out.println(logPosition());

//            for (ConsumerRecord<String , String> rec : records) {
            if (logOn) {
                for (ConsumerRecord<WindowedPageViewByRegion, RegionCount> rec : records) {

                    Date dt = new Date(rec.key().windowStart);
                    Date dt1 = new Date();
                    System.out.println("(Key region:" + rec.key().region + "  Key windowStart:" + dt.toString() + "||||" + dt1.toString() + ")    (Value reg:" + rec.value().region + "  Value  count:" + rec.value().count + ")" + rec.offset());
//                    System.out.println("(Key region:" + rec.key() + ")    (Value reg:" + rec.value()+ ")" + rec.offset());
                    System.out.println("------------------*****---------------------");
                    partitionBenchMark(rec);
                }
            }
            if (!logOn) {
                for (ConsumerRecord<WindowedPageViewByRegion, RegionCount> rec : records) {
                    partitionBenchMark(rec);
                }
            }

        }

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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "windowing");
//        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, (4000 * 10000) + "");//change this for increase and decrease packet fethe by consumer every message is 100Byte
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_DOC, "earliest");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_DOC, "latest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "com.datis.irc.pojo.WindowDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.datis.irc.pojo.RegionCountDeserializer");
//        ConsumerRegion consumer1 = new ConsumerRegion("step2test1", props, true);
        ConsumerRegionWindowing consumer1 = new ConsumerRegionWindowing("step3", props, true);
        consumer1.start();
    }
}
