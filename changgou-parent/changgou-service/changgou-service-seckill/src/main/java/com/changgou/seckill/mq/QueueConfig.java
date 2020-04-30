package com.changgou.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class QueueConfig {

    //创建超时队列
    @Bean
    public Queue seckillQueue(){
        return new Queue("seckillQueue");
    }

    //创建延时队列
    @Bean
    public Queue delayseckillQueue(){
        return QueueBuilder.durable("delayseckillQueue")
                .withArgument("x-dead-letter-exchange", "seckillExchange") //当前队列的消息，一旦过期，则进入死信交换机
                .withArgument("x-dead-letter-routing-key", "seckillQueue")
                .build();
    }

    /**
     * 秒杀交换机
     * @return
     */
    @Bean
    public Exchange seckillExchange(){
        return new DirectExchange("seckillExchange");
    }

    /**
     * 队列绑定交换机
     */
    @Bean
    public Binding seckillQueueBingExchange(Queue seckillQueue, Exchange seckillExchange){
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }
}
