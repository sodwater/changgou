package com.changgou.order.controller;


import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;
    /**
     *加入购物车
     * 1:加入购物车数量
     * 2：商品ID
     */
    @GetMapping(value = "/add")
    public Result add(Integer num, Long id){
        //获取用户得登录名
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");

        cartService.add(num, id, username);
        return new Result(true, StatusCode.OK, "加入成功");
    }

    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
        //获取用户得登录名
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");

        //String username = "szitheima";
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<List<OrderItem>>(true, StatusCode.OK, "查询成功", orderItems);
    }



}
