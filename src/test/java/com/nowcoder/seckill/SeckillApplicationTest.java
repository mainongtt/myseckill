package com.nowcoder.seckill;

import com.nowcoder.seckill.entity.User;
import com.nowcoder.seckill.service.UserService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.messaging.support.MessageBuilder;

//@SpringBootTest
class SeckillApplicationTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testRedisTemplate() {
        redisTemplate.opsForValue().set("name:1", "Tom");
        System.out.println(redisTemplate.opsForValue().get("name:1"));
        redisTemplate.opsForValue().set("user:15", userService.findUserById(15));
        User user = (User) redisTemplate.opsForValue().get("user:15");
        System.out.println(user);
    }

    @Test
    public void testTransactional() {
        Object result = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi();
                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                System.out.println(operations.opsForSet().members(redisKey));

                return operations.exec();
            }
        });
        System.out.println(result);
    }

    @Test
    public void testRocketMQ() throws InterruptedException {
        rocketMQTemplate.sendMessageInTransaction(
                "seckill:decrease_stock", MessageBuilder.withPayload("decrease stock 1").build(), null);
        rocketMQTemplate.sendMessageInTransaction(
                "seckill:decrease_stock", MessageBuilder.withPayload("decrease stock 2").build(), null);
        rocketMQTemplate.sendMessageInTransaction(
                "seckill:decrease_stock", MessageBuilder.withPayload("decrease stock 3").build(), null);

        Thread.sleep(300 * 1000);
    }

}
