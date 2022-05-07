package com.nowcoder.seckill;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

//@SpringBootApplication
public class RocketMQTestInSpring implements CommandLineRunner {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(RocketMQTestInSpring.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        testProduce();
        testProduceInTransaction();
    }

    private void testProduce() throws Exception {
        for (int i = 0; i < 10; i++) {
            String destination = "seckillTest:tag" + (i % 2);
            Message message = MessageBuilder.withPayload("message " + i).build();
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {

                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("SUCCESS: " + sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    e.printStackTrace();
                }
            }, 3000);
        }
    }

    private void testProduceInTransaction() throws Exception {
        String destination = "seckillTest:tagT";
        for (int i = 0; i < 10; i++) {
            Message message = MessageBuilder.withPayload("message " + i).build();
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(destination, message, null);
            System.out.println(sendResult);
        }
    }

    @RocketMQTransactionListener
    private class TransactionListenerImpl implements RocketMQLocalTransactionListener {

        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            System.out.println("executeLocalTransaction: " + msg + ", " + arg);
            return RocketMQLocalTransactionState.COMMIT;
        }

        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            System.out.println("checkLocalTransaction: " + msg);
            return RocketMQLocalTransactionState.COMMIT;
        }
    }

    @Service
    @RocketMQMessageListener(topic = "seckillTest",
            consumerGroup = "seckill_consumer_0", selectorExpression = "tag0")
    private class StringConsumer0 implements RocketMQListener<String> {

        @Override
        public void onMessage(String message) {
            System.out.println("StringConsumer0: " + message);
        }
    }

    @Service
    @RocketMQMessageListener(topic = "seckillTest",
            consumerGroup = "seckill_consumer_1", selectorExpression = "tag1")
    private class StringConsumer1 implements RocketMQListener<String> {

        @Override
        public void onMessage(String message) {
            System.out.println("StringConsumer1: " + message);
        }
    }

    @Service
    @RocketMQMessageListener(topic = "seckillTest",
            consumerGroup = "seckill_consumer_x", selectorExpression = "*")
    private class StringConsumerX implements RocketMQListener<String> {

        @Override
        public void onMessage(String message) {
            System.out.println("StringConsumerX: " + message);
        }
    }

}
