package com.changgou.pay.controller;


import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 支付结果回调方法
     */
    @RequestMapping(value = "/notify/url")
    public String notifyurl(HttpServletRequest request) throws Exception {
        //获取网络输入流
        ServletInputStream inputStream = request.getInputStream();

        //创建一个流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        baos.close();
        inputStream.close();
        //微信支付结果的字节数组
        byte[] bytes = baos.toByteArray();
        //微信支付系统传递过来的XML的字符串
        String resultStrXML = new String(bytes, "utf-8");
        //3.转成MAP
        Map<String, String> map = WXPayUtil.xmlToMap(resultStrXML);

        System.out.println(map);
        //获取自定义参数
        String attach = map.get("attach");
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

        //发送支付结果给MQ
        rabbitTemplate.convertAndSend(attachMap.get("exchange"), attachMap.get("routingkey"), JSON.toJSONString(map));

        String result = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

        return result;
    }


    /**
     * 根据交易订单号 来查询订单的状态
     *
     * @param outtradeno
     * @return
     */
    @GetMapping("/status/query")
    public Result<Map> queryStatus(String outtradeno) {
        Map map = weixinPayService.queryStatus(outtradeno);
        return new Result<Map>(true, StatusCode.OK, "查询状态OK", map);
    }


    /**
     * 创建二维码
     * 普通订单：
     *      exchange：exchange.order
     *                queue.order
     * 秒杀订单：
     *      exchange.seckillorder
     *               seckillorder
     * @param parameterMap
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String, String> parameterMap) {
        //创建二维码
        Map<String, String> resultMap = weixinPayService.createnative(parameterMap);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功！", resultMap);
    }
}
