package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 秒杀订单监听
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    //消息队列
    @RabbitHandler
    public void getMessage(String message){
        //将支付信息转成map
        try {
            Object o = JSON.parse(message);
            Map<String, String> resultMap = (Map<String, String>) o;
//            Map<String, String> resultMap = WXPayUtil.xmlToMap(message);

            String return_code = resultMap.get("return_code");
            String outTradeNo = resultMap.get("out_trade_no");//订单号
            String attach = resultMap.get("attach"); // 自定义数据
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

            if (return_code.equals("SUCCESS")){
                String result_code = resultMap.get("result_code");//
                if (result_code.equals("SUCCESS")){
                    //修改订单状态
                    seckillOrderService.updatePayStatus(resultMap.get("time_end") , resultMap.get("transaction_id"), attachMap.get("username"));
                    //清理用户排队订单
                }else {
                    //删除订单
                    seckillOrderService.deleteOrder(attachMap.get("username"));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
