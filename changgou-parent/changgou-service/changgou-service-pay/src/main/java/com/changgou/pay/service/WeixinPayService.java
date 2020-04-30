package com.changgou.pay.service;

import java.util.Map;

public interface WeixinPayService {


    /**
     * 查询微信支付状态
     */
    Map queryStatus(String outtradeno);

    /**
     * 获取二维码
     */
    Map createnative(Map<String, String> parameterMap);
}
