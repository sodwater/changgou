package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 多线程下单操作
     */
    @Async
    public void createOrder(){
        try {

            //从redis队列中获取用户排队信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            if (seckillStatus == null){
                return;
            }

            //先到队列中获取该商品的信息，如果能获取，则可以下单
            Object sgoods = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            if (sgoods == null){
                //如果不能获取该商品的列队信息。则表示没有库存，清理排队信息
                clearUserQueue(seckillStatus.getUsername());
                return;
            }


            System.out.println("======多线程开始");
            String time = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();
            String username = seckillStatus.getUsername();

            //查询秒杀商品
            String key = "SeckillGoods_" + time;
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(key).get(id);

            //判断是否有库存
            System.out.println(seckillGoods.getStockCount());
            if (seckillGoods==null || seckillGoods.getStockCount()<=0){
                throw new RuntimeException("已售馨");
            }

            //创建订单对象
            SeckillOrder seckillOrder = new SeckillOrder();
            //将订单对象存起来
            seckillOrder.setSeckillId(id); //商品ID
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setMoney(seckillGoods.getCostPrice()); //支付金额
            seckillOrder.setUserId(username); //用户名
            seckillOrder.setCreateTime(new Date()); //创建时间
            seckillOrder.setStatus("0"); //未支付

            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            /**
             * 库存递减
             */
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

            //获取该商品的队列数量
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).size();

            if (size<=0){
                //同步数量
                seckillGoods.setStockCount(size.intValue());
                //同步数据到mysql
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //删除redis中的数据
                redisTemplate.boundHashOps(key).delete(id);
            }else {
                //同步到redis
                redisTemplate.boundHashOps(key).put(id, seckillGoods);
            }
            seckillStatus.setOrderId(seckillOrder.getId()); // id
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice())); //价格
            seckillStatus.setStatus(2);  //待付款
            // 创建抢单查询队列
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            System.out.println("=====开始发送延时队列");

            //发送消息给延迟队列
            rabbitTemplate.convertAndSend("delayseckillQueue", (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("100000");//增加延时
                    return message;
                }
            });
            System.out.println("==== =  下单完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**\
     * 清理用户排队抢单信息
     */
    public void clearUserQueue(String username){
        //排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //排队信息清理
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }
}