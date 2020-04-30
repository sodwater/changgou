package com.changgou.order.service;

import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import entity.Result;

import java.util.List;

public interface CartService {

    /**
     * 加入购物车
     */
    void add(Integer num, Long id, String username);

    /**
     * 购物车集合查询
     * @param username
     * @return
     */
    List<OrderItem> list(String username);
}
