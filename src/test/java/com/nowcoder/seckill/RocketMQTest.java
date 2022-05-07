package com.nowcoder.seckill;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.Charset;
import java.util.List;

public class RocketMQTest {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
//        testDefaultMQProducer();
//        testDefaultMQConsumer();
//        testTransactionMQProducer();
    }

    public static void testDefaultMQProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("seckill_producer");
        producer.setNamesrvAddr("139.9.119.64:9876");
        producer.start();

        String topic = "seckillTest";
        String tag = "tag1";
        for (int i = 0; i < 100; i++) {
            String body = "message " + i;
            Message message = new Message(topic, tag, body.getBytes(UTF_8));
            SendResult result = producer.send(message);
            System.out.println(result);
        }
    }

    public static void testDefaultMQConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("seckill_consumer");
        consumer.setNamesrvAddr("139.9.119.64:9876");
        consumer.subscribe("seckillTest", "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.println(Thread.currentThread().getName() + ": " + msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

    public static void testTransactionMQProducer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("seckill_producer");
        producer.setNamesrvAddr("139.9.119.64:9876");
        producer.setTransactionListener(new TransactionListenerImpl());
        producer.start();

        String topic = "seckillTest";
        String tag = "tag2";
        for (int i = 0; i < 10; i++) {
            String body = "message " + i;
            Message message = new Message(topic, tag, body.getBytes(UTF_8));
            SendResult result = producer.sendMessageInTransaction(message, null);
            System.out.println(result);
        }
    }

    static class TransactionListenerImpl implements TransactionListener {

        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            System.out.println("executeLocalTransaction: " + msg + ", " + arg);
            return LocalTransactionState.UNKNOW;
        }

        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            System.out.println("checkLocalTransaction: " + msg);
            return LocalTransactionState.UNKNOW;
        }
    }

}
